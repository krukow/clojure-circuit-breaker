(ns net.higher-order.integration.circuit-breaker.AtomicCircuitBreaker
  (:require [net.higher-order.integration.circuit-breaker.atomic :as a]
	    [net.higher-order.integration.circuit-breaker.states :as s])
  (:import net.higher_order.integration.circuit_breaker.CircuitBreaker
	   net.higher_order.integration.circuit_breaker.states.CircuitBreakerTransitions
	   net.higher_order.integration.circuit_breaker.states.TransitionPolicy)
  (:gen-class
   :init init
   :prefix "impl-"
   :implements [net.higher_order.integration.circuit_breaker.CircuitBreaker]
   :constructors {[] []
		  [net.higher_order.integration.circuit_breaker.states.CircuitBreakerTransitions] []}
   :state state))


(defn impl-init 
  ([] [[] (a/make-circuit-breaker)])
  ([st] [[] (a/make-circuit-breaker-from-state st)]))


(defn impl-wrap [c f]
  (a/wrap-with f (.state c)))


(defn #^net.higher_order.integration.circuit_breaker.states.CircuitBreakerTransitions impl-current_state [this]
  @(.state this))