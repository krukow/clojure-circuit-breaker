(ns net.higher-order.integration.circuit-breaker.java
  (:require [net.higher-order.integration.circuit-breaker.states :as s]))


(gen-interface
 :name net.higher_order.integration.circuit_breaker.java.CircuitBreaker
 :methods [ [wrap [clojure.lang.IFn] clojure.lang.IFn]
	    [getCurrentState [] net.higher_order.integration.circuit_breaker.states.CircuitBreakerTransitions]])
