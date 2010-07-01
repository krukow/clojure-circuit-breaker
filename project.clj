(defproject org.clojars.krukow/circuit-breaker "1.0.0-SNAPSHOT"
  :description "Functional implementation of Nygaards Circuit-breaker using Clojure protocols"
  :namespaces [net.higher-order.integration.circuit-breaker.atomic
	       net.higher-order.integration.circuit-breaker.states
	       net.higher-order.integration.circuit-breaker
	       net.higher-order.integration.circuit-breaker.AtomicCircuitBreaker]
  
  :dependencies [[org.clojure/clojure                  "1.2.0-master-SNAPSHOT"]
                 [org.clojure/clojure-contrib          "1.2.0-master-SNAPSHOT"]]
  
  :dev-dependencies [[autodoc              "0.7.0"]
                     [leiningen/lein-swank "1.2.0-SNAPSHOT"]])