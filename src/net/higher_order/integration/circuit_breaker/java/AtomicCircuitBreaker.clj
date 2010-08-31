(ns net.higher-order.integration.circuit-breaker.java.AtomicCircuitBreaker
  (:require [net.higher-order.integration.circuit-breaker.atomic :as a]
	    [net.higher-order.integration.circuit-breaker.states :as s]
	    [net.higher-order.integration.circuit-breaker.java :as j])
  (:import [net.higher_order.integration.circuit_breaker.java CircuitBreaker])
  (:gen-class
    :name net.higher_order.integration.circuit_breaker.java.AtomicCircuitBreaker
    :init init
    :prefix "impl-"
    :implements [net.higher_order.integration.circuit_breaker.java.CircuitBreaker]
    :constructors {[] []
		   [net.higher-order.integration.circuit-breaker.states.TransitionPolicy] []}
    :state state))


(defn impl-init 
  ([] [[] (a/make-circuit-breaker)])
  ([p] [[] (a/make-circuit-breaker p)]))


(defn impl-wrap [^net.higher_order.integration.circuit_breaker.java.AtomicCircuitBreaker c f]
  (a/wrap-with f (.state c)))

(defn impl-resetState [c s] s)

(defn ^net.higher_order.integration.circuit_breaker.states.CircuitBreakerTransitions
  impl-getCurrentState
  [^net.higher_order.integration.circuit_breaker.java.AtomicCircuitBreaker this]
  (let [st @(.state this)]
    (if (instance? net.higher_order.integration.circuit_breaker.states.CircuitBreakerTransitions
		   st)
      st
      (reify net.higher_order.integration.circuit_breaker.states.CircuitBreakerTransitions
	     (proceed [s] (s/proceed st))
	     (on-success [s] (s/on-success st))
	     (on-error [s]  (s/on-error st))
	     (on-before-call [s] (s/on-before-call st))))))