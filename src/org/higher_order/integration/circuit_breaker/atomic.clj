(comment
 "Implementation of Michael Nygaard's Circuit breaker stability pattern.

A circuit breaker acts as an intemediary between a caller and a callee.
Typically the callee is an interface to an integration point, and
the caller is a client of that integration point.

If the circuit breaker detects a number of failures in the callee it 
'trips' (i.e., transitions to the open state) and prevents further 
calls to the callee for a fixed period (fail fast).

After this period it lets a single call go though and transitions
to the initial state or back to the open state depending on the outcome.")

(comment
  (add-classpath "file:///Users/krukow/Projects/clojure/circuitbreaker/src/"))

(ns org.higher-order.integration.circuit-breaker.atomic
  (:use org.higher-order.integration.circuit-breaker.states)
  (:gen-class))

(def default-policy (TransitionPolicy 5 5000))
(def initial-state (ClosedState default-policy 0))

(def state (atom initial-state))

(def transition-by!)

(defn wrap [f]
  (fn [& args]
    (let [s (transition-by! on-before-call)]
      (if (proceed s)
	(try 
	 (let [res (apply f args)]
	   (do (transition-by! on-success)
	       res))
	 (catch Exception e
	   (do 
	     (transition-by! on-error)
	     (throw e))))
	(throw (java.lang.RuntimeException. "OpenCircuit"))))))

(defn transition-by! [f]
  (loop [s @state
	 t (f s)]
    (cond 
      (identical? s t) s
      (compare-and-set! state s t) t
      :else (let [s1 @state
		  t1 (f s1)]
	      (recur s1 t1)))))


(comment test

(def #^{:private true}s (wrap (constantly 42)))
(def #^{:private true}f (wrap (fn [_] (assert nil))))

(dotimes [i 10]
  (s))

(assert (= (ClosedState default-policy 0) @state))

(dotimes [i 5]
  (try (f) (catch Exception e)))

(assert (= (ClosedState default-policy 5) @state))

(try (f) (catch Exception e))

(assert (= (class (OpenState default-policy 0)) (class @state)))
(Thread/sleep 5000)
(s)
(assert (= (ClosedState default-policy 0) @state))


)