(comment
"Implementation of Michael Nygaard's circuit breaker stability pattern.

A circuit breaker acts as an intemediary between a caller and a callee.
Typically the callee is an interface to an integration point, and
the caller is a client of that integration point.

If the circuit breaker detects a number of failures in the callee it 
'trips' (i.e., transitions to the open state) and prevents further 
calls to the callee for a fixed period (fail fast).

After this period it lets a single call go though and transitions
to the initial state or back to the open state depending on the outcome.

The circuit breaker is parameterized by a policy that decides
* how many failures are needed to trip
* how long before letting the single probe call pass
* what type of Exceptions from the integration point should
be considered errors to the circuit breaker
(e.g., one may want to exclude security related exceptions)")

(ns net.higher-order.integration.circuit-breaker.atomic
  (:require [net.higher-order.integration.circuit-breaker.states :as s])
  (:import [net.higher-order.integration.circuit-breaker.states
	    TransitionPolicy]))

(def default-policy (s/make-transition-policy 5 5000))
(def initial-state (s/mk-closed default-policy 0))

(defn wrap-with [f state]
  (fn [& args]
    (let [s (swap! state s/on-before-call)]
      (if (s/proceed s)
	(try
	  (let [res (apply f args)]
	    (do (swap! state s/on-success)
		res))
	 (catch Exception e
	   (do
	     (swap! state
		    (fn [s]
		      ((if (:is-error (:policy @state) e)
			 s/on-error
			 s/on-success) s)))
	     (throw e))))
	(throw (RuntimeException. "OpenCircuit"))))))


(defn make-circuit-breaker
  "Creates a circuit-breaker instance. If called with no arguments
a circuit-breaker in the initial closed state with the default policy is
created. If called with one argument supporting the CircuitBreakerTransitions
protocol, a circuit-breaker is created using that as state."
  ([] (atom initial-state))
  ([p] {:pre [(instance? TransitionPolicy p)]}
     (atom (s/mk-closed p 0))))

(defn make-circuit-breaker-from-state [s]
  {:pre [(satisfies? s/CircuitBreakerTransitions s)]}
  (atom s))

(comment 
  test
  (def cb (make-circuit-breaker))
  (def succ (wrap-with (constantly 42) cb))
  (def fail (wrap-with (fn [] (throw (Exception.))) cb))

  (dotimes [i 10]
    (succ))

  (assert (= (s/mk-closed default-policy 0) @cb))

  (dotimes [i 5]
    (try (fail) (catch Exception e)))

  (assert (= (s/mk-closed default-policy 5) @cb))

  (try (fail) (catch Exception e))

  (assert (= (class (s/mk-open default-policy 1)) (class @cb)))
  (Thread/sleep 5000)
  (succ)
  (assert (= (s/mk-closed default-policy 0) @cb))

)