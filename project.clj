(defproject drunken-dangerzone "0.1.0-SNAPSHOT"
  :description "Simple in-memory key-value store with a REST API."
  :url "http://github.com/kisom/clj-drunken-dangerzone"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [hiccup "1.0.3"]
                 [cheshire "5.1.0"]
                 [compojure "1.1.5"]]
  :plugins [[lein-ring "0.8.2"]]
  :ring {:handler drunken-dangerzone.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})
