(ns org.higher-order.integration.circuit-breaker.states
  (:gen-class))

(deftype TransitionPolicy [fail-count timeout])

(defprotocol CircuitBreakerTransitions 
  "Transition functions for circuit-breaker states"
  (proceed [s] "true if breaker should proceed with call in this state")
  (on-success [s] "transition from s to this state after a successful call")
  (on-error [s] "transition from s to this state after an unsuccessful call")
  (on-before-call [s] "transition from s to this state before a call")
  (on-cancel [s] "transition from s to this state after a call is cancelled by circuit breaker")
  (with-transition-policy [s p] "create a state like s but with policy p"))

(deftype ClosedState [policy fail-count] [clojure.lang.IPersistentMap])
(deftype OpenState [policy time-stamp]   [clojure.lang.IPersistentMap])
(deftype InitialHalfOpenState [policy]   [clojure.lang.IPersistentMap])
(deftype PendingHalfOpenState [policy]   [clojure.lang.IPersistentMap])

(defn- tt [s] true)
(defn- ff [s] false)
(defn- gettime [] (System/currentTimeMillis))

(def #^{:private true}abs-transitions
  {:proceed ff
   :on-success identity
   :on-error identity
   :on-before-call identity
   :on-cancel identity
   :with-transition-policy (fn [s p] (assoc s :policy p))})

(extend 
 ::ClosedState CircuitBreakerTransitions
 (merge abs-transitions
	{:proceed tt
	 :on-success (fn [s] (assoc s :fail-count 0))
	 :on-error (fn [s]
		     (let [p (:policy s)
			   f (:fail-count s)]
		       (if (= f (:fail-count p))
			 (OpenState p (gettime))
			 (ClosedState p (inc f)))))}))

(extend 
 ::OpenState
 CircuitBreakerTransitions
 (merge abs-transitions
	{:proceed ff
	 :on-success (fn [s] (InitialHalfOpenState (:policy s)))
	 :on-before-call (fn [s] 
			   (let [p (:policy s)]
			     (if (> (- (System/currentTimeMillis) (:time-stamp s))
				    (:timeout p)) 
			       (InitialHalfOpenState p)
			       s)))}))

(extend 
 ::InitialHalfOpenState
 CircuitBreakerTransitions
 (merge abs-transitions
	{:proceed tt
	 :on-before-call (fn [s] (PendingHalfOpenState (:policy s)))
	 :on-success (fn [s] (ClosedState (:policy s) 0))
	 :on-error (fn [s] (OpenState (:policy s) (gettime)))}))

(extend 
 ::PendingHalfOpenState
 CircuitBreakerTransitions
 (merge abs-transitions
	{:on-success (fn [s] (ClosedState (:policy s) 0))
	 :on-error (fn [s] (OpenState (:policy s) (gettime)))}))


(comment test

(def default-policy (TransitionPolicy 5 5000))

(def initial-state (ClosedState default-policy 0))

(assert (= initial-state (on-success initial-state)))

(assert (= (ClosedState default-policy 1) (on-error initial-state)))

(def e4 (ClosedState default-policy 4))

(assert (= (class (OpenState default-policy (gettime))) 
	   (class (on-error (on-error e4)))))

(let [s (on-error (on-error e4))
      o (on-cancel s)
      oclass (class (OpenState default-policy (gettime)))]
  (do
    (assert (= (class o) oclass))
    (Thread/sleep 6000)
    (assert (= (class (on-cancel s))
	       (class (InitialHalfOpenState default-policy))))))


(def i (InitialHalfOpenState default-policy))

(assert (= (on-before-call i) (PendingHalfOpenState default-policy)))
(assert (= (class (on-error i)) (class (OpenState default-policy (gettime)))))
(assert (= (on-success i) initial-state))

(def p (PendingHalfOpenState default-policy))
(assert (= (on-success p) initial-state))
)
