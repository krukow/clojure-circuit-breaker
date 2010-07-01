(ns net.higher-order.integration.circuit-breaker)

(defprotocol CircuitBreaker
  (#^clojure.lang.IFn wrap [this #^clojure.lang.IFn fn])
  (#^net.higher_order.integration.circuit_breaker.states.CircuitBreakerTransitions current-state [this]))
  