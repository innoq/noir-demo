(defproject noir-demo "0.1.0-SNAPSHOT"
            :description "very basic todo app"
            :dependencies [[org.clojure/clojure "1.3.0"]
                           [noir "1.2.1"]
                           [korma "0.3.0-beta9"]
                           [org.hsqldb/hsqldb "2.2.8"]
                           [clj-stacktrace "0.2.4"]]          ; workaround for clojure-jack-in problem
            :dev-dependencies [[lein-marginalia "0.7.0"]]
            :main noir-demo.server)

