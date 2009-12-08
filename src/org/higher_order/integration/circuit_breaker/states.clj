(ns org.higher-order.integration.circuit-breaker.states
  (:gen-class))

(deftype TransitionPolicy [fail-count timeout])

(defprotocol CircuitBreakerTransitions 
  "Transition functions for circuit-breaker states"
  (proceed [s] "true if breaker should proceed with call in this state")
  (on-success [s] "transition from s to this state after a successful call")
  (on-error [s] "transition from s to this state after an unsuccessful call")
  (on-before-call [s] "transition from s to this state before a call"))

(deftype ClosedState [policy fail-count] clojure.lang.IPersistentMap)
(deftype OpenState [policy time-stamp]   clojure.lang.IPersistentMap)
(deftype InitialHalfOpenState [policy]   clojure.lang.IPersistentMap)
(deftype PendingHalfOpenState [policy]   clojure.lang.IPersistentMap)


(def #^{:private true}
     abs-transitions
     {:proceed (constantly false)
      :on-success identity
      :on-error identity
      :on-before-call identity})

(defn- to-closed-state-from [s] (ClosedState (:policy s) 0))

(extend 
 ::ClosedState CircuitBreakerTransitions
 (merge abs-transitions
	{:proceed    (constantly true)
	 :on-success (fn [s] (if (zero? (:fail-count s)) s (to-closed-state-from s)))
	 :on-error   (fn [s]
		       (let [p (:policy s)
			     f (:fail-count s)]
			 (if (= f (:fail-count p))
			   (OpenState p (System/currentTimeMillis))
			   (assoc s :fail-count (inc f)))))}))

(extend 
 ::OpenState CircuitBreakerTransitions
 (merge abs-transitions
	{:on-success (fn [s] (InitialHalfOpenState (:policy s)))
	 :on-before-call (fn [s] 
			   (let [p (:policy s)
				 delta (- (System/currentTimeMillis) (:time-stamp s))]
			     (if (> delta (:timeout p)) 
			       (InitialHalfOpenState p)
			       s)))}))

(extend 
 ::InitialHalfOpenState CircuitBreakerTransitions
 (merge abs-transitions
	{:proceed (constantly true)
	 :on-before-call (fn [s] (PendingHalfOpenState (:policy s)))
	 :on-success     to-closed-state-from
	 :on-error       (fn [s] (OpenState (:policy s) (System/currentTimeMillis)))}))

(extend 
 ::PendingHalfOpenState
 CircuitBreakerTransitions
 (merge abs-transitions
	{:on-success to-closed-state-from
	 :on-error (fn [s] (OpenState (:policy s) (System/currentTimeMillis)))}))


(comment test

(def #^{:private true}default-policy (TransitionPolicy 5 5000))
(def #^{:private true}initial-state (ClosedState default-policy 0))

(assert (= initial-state (on-success initial-state)))

(assert (= (ClosedState default-policy 1) (on-error initial-state)))

(def #^{:private true}e4 (ClosedState default-policy 4))

(assert (= (class (OpenState default-policy (System/currentTimeMillis))) 
	   (class (on-error (on-error e4)))))

(let [s (on-error (on-error e4))
      o (on-before-call s)
      oclass (class (OpenState default-policy (System/currentTimeMillis)))]
  (do
    (assert (= (class o) oclass))
    (Thread/sleep 6000)
    (assert (= (class (on-before-call s))
	       (class (InitialHalfOpenState default-policy))))))


(def #^{:private true}i (InitialHalfOpenState default-policy))

(assert (= (on-before-call i) (PendingHalfOpenState default-policy)))
(assert (= (class (on-error i)) (class (OpenState default-policy (System/currentTimeMillis)))))
(assert (= (on-success i) initial-state))

(def #^{:private true}p (PendingHalfOpenState default-policy))
(assert (= (on-success p) initial-state))
)
