(defproject org.clojars.krukow/circuit-breaker "1.0.0-SNAPSHOT"
  :description "Functional implementation of Nygaards Circuit-breaker using Clojure protocols"
  :url "http://example.org/sample-clojure-project"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  
  ;; Emit warnings on all reflection calls.
  :warn-on-reflection true
  :jar-dir "target/" ; where to place the project's jar file
  :namespaces [net.higher-order.integration.circuit-breaker.atomic
	       net.higher-order.integration.circuit-breaker.states
	       net.higher-order.integration.circuit-breaker.java
	       net.higher-order.integration.circuit-breaker.java.AtomicCircuitBreaker]
	       
  
  :dependencies [[org.clojure/clojure                  "1.2.0-master-SNAPSHOT"]
                 [org.clojure/clojure-contrib          "1.2.0-master-SNAPSHOT"]]
  
  :dev-dependencies [[autodoc              "0.7.0"]
                     [leiningen/lein-swank "1.2.0-SNAPSHOT"]])