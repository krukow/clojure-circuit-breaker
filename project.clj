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
  :namespaces [net.higher-order.integration.circuit-breaker.states
	       net.higher-order.integration.circuit-breaker.atomic
	       net.higher-order.integration.circuit-breaker.java
	       net.higher-order.integration.circuit-breaker.java.AtomicCircuitBreaker]

  :dependencies [[org.clojure/clojure                  "1.2.0"]
		 [org.clojure/clojure-contrib          "1.2.0"]]

  :dev-dependencies [[swank-clojure "1.3.0-SNAPSHOT"]
		     [cdt "1.0.1-SNAPSHOT"]]

  :jvm-opts
  ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8030"])