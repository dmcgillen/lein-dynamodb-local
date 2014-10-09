(defproject lein-dynamodb-local "0.1.0-SNAPSHOT"
  :description "Lein plugin to provide a local DynamoDB instance to run tests against"
  :url "https://github.com/dmcgillen/lein-dynamodb-local"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :dependencies [[net.lingala.zip4j/zip4j "1.3.2"]]
  :plugins [[lein-release "1.0.5"]]
  :lein-release {:deploy-via :clojars
                 :clojars-url "clojars@clojars.org:"})
