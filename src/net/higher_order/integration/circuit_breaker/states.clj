(ns net.higher-order.integration.circuit-breaker.states)

(defprotocol CircuitBreakerTransitions
  "Transition functions for circuit-breaker states"
  (proceed [s] "true if breaker should proceed with call in this state")
  (on-success [s] "transition from s to this state after a successful call")
  (on-error [s] "transition from s to this state after an unsuccessful call")
  (on-before-call [s] "transition from s to this state before a call"))

(defprotocol TransitionPolicy
  (max-fail-count [p] "returns the maximum number of
failed calls before transitioning from closed to open")
  (timeout [p] "returns the number of miliseconds to wait in open state")
  (is-error [p ex] "returns true if exception ex should be considered an error.
Some exceptions, e.g., access control violation, are not a sign of integration-point
      failure."))

(defrecord SimpleTransitionPolicy [max-fail timeoutms]
  TransitionPolicy
  (max-fail-count [this] max-fail)
  (timeout [this] timeoutms)
  (is-error [p ex] true))

(defn make-transition-policy [max-fail timeout]
  {:pre [(and (pos? max-fail) (pos? timeout))]}
  (SimpleTransitionPolicy. max-fail timeout))

(declare mk-closed mk-open mk-initial-half-open mk-pending-half-open)

(defrecord ClosedState [#^{:tag (:on-interface TransitionPolicy)} policy ^int fail-count]
  CircuitBreakerTransitions
  (proceed [this] true)
  (on-success [this]
	      (if (zero? fail-count) this (ClosedState. policy 0)))
  (on-error [this]
	    (if (= fail-count (max-fail-count policy))
	      (mk-open policy (System/currentTimeMillis))
	      (assoc this :fail-count (inc fail-count))))
  (on-before-call [this] this))

(defn mk-closed [^TransitionPolicy policy fail-count]
  {:pre [(not (neg? fail-count))]}
  (ClosedState. policy fail-count))

(defrecord OpenState [#^{:tag (:on-interface TransitionPolicy)} policy ^long time-stamp]
  CircuitBreakerTransitions
  (proceed [_] false)
  (on-success [this] (mk-initial-half-open policy))
  (on-error [this] this)
  (on-before-call
   [this] 
   (let [delta (- (System/currentTimeMillis) time-stamp)]
     (if (> delta (timeout policy)) 
       (mk-initial-half-open policy)
       this))))

(defn mk-open [^TransitionPolicy policy time-stamp]
  {:pre [(pos? time-stamp)]}
  (OpenState. policy time-stamp))

(defrecord InitialHalfOpenState [#^{:tag (:on-interface TransitionPolicy)} policy]
  CircuitBreakerTransitions
  (proceed [this] true)
  (on-success [this] (mk-closed policy 0))
  (on-error [this] (mk-open policy (System/currentTimeMillis)))
  (on-before-call [this] (mk-pending-half-open policy)))

(defn mk-initial-half-open [^TransitionPolicy policy]
  (InitialHalfOpenState. policy))

(defrecord PendingHalfOpenState [#^{:tag (:on-interface TransitionPolicy)} policy]
  CircuitBreakerTransitions
  (proceed [this] false)
  (on-success [this] (mk-closed policy 0))
  (on-error [this] (mk-open policy (System/currentTimeMillis)))
  (on-before-call [this] this))

(defn mk-pending-half-open [^TransitionPolicy policy]
  (PendingHalfOpenState. policy))



(comment test

	 (def #^{:private true}default-policy (make-transition-policy 5 5000))
	 (def #^{:private true}initial-state (mk-closed default-policy 0))
	 
	 (assert (= initial-state (on-success initial-state)))

	 (assert (= (mk-closed default-policy 1) (on-error initial-state)))

	 (def #^{:private true}e4 (mk-closed default-policy 4))

	 (assert (= OpenState
		    (class (on-error (on-error e4)))))

	 (let [s (on-error (on-error e4))
	       o (on-before-call s)]
	   (do
	     (assert (= (class o) OpenState))
	     (Thread/sleep (+ (timeout default-policy) 1000))
	     (assert (= (class (on-before-call s))
			InitialHalfOpenState))))


	 (def #^{:private true}i (mk-initial-half-open default-policy))

	 (assert (= (on-before-call i) (mk-pending-half-open default-policy)))
	 (assert (= (class (on-error i)) OpenState))
	 (assert (= (on-success i) initial-state))

	 (def #^{:private true}p (mk-pending-half-open default-policy))
	 (assert (= (on-success p) initial-state))
	 (assert (= (class (on-error p)) OpenState))

	 )
