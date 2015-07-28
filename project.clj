(defproject lein-dynamodb-local "0.2.2-SNAPSHOT"
  :description "A Leiningen plugin for providing a local DynamoDB instance"
  :url "https://github.com/dmcgillen/lein-dynamodb-local"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :eval-in-leiningen true
  :dependencies [[commons-io/commons-io "2.4"]
                 [net.lingala.zip4j/zip4j "1.3.2"]]
  :plugins [[lein-release "1.0.5"]]
  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :creds :gpg}]])
