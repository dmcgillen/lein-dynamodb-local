(ns leiningen.unit.dynamodb-local
  (:require [leiningen.dynamodb-local :refer :all]
            [environ.core :refer [env]]
            [midje
             [sweet :refer :all]
             [util :refer [testable-privates]]])
  (:import [java.io File]))

(testable-privates leiningen.dynamodb-local build-dynamo-command ensure-installed)

(def ^:private dynamo-directory (str (System/getProperty "user.home") File/separator ".lein-dynamodb-local"))

(fact-group
 :unit

 (fact "Build dynamo command uses defaults if no config is given in the project map"
       (let [project {:some-key "some-val"}]
         (build-dynamo-command project) => (str "java  -Djava.library.path=" dynamo-directory "/DynamoDBLocal_lib -jar " dynamo-directory "/DynamoDBLocal.jar -port 8000 -dbPath " dynamo-directory)))

 (fact "Build dynamo command allows port to be specified in the project map"
       (let [project {:some-key "some-val"
                      :dynamodb-local {:port "9999"}}]
         (build-dynamo-command project) => (str "java  -Djava.library.path=" dynamo-directory "/DynamoDBLocal_lib -jar " dynamo-directory "/DynamoDBLocal.jar -port 9999 -dbPath " dynamo-directory)))

 (fact "Build dynamo command allows port to be specified as an environment variable"
       (let [project {:some-key "some-val"}]
         (build-dynamo-command project) => (str "java  -Djava.library.path=" dynamo-directory "/DynamoDBLocal_lib -jar " dynamo-directory "/DynamoDBLocal.jar -port 7777 -dbPath " dynamo-directory)
         (provided (env :dynamodb-port "8000") => "7777")))

 (fact "Build dynamo command takes port from the project map in precedence over an environment variable"
       (let [project {:some-key "some-val"
                      :dynamodb-local {:port "9999"}}]
         (build-dynamo-command project) => (str "java  -Djava.library.path=" dynamo-directory "/DynamoDBLocal_lib -jar " dynamo-directory "/DynamoDBLocal.jar -port 9999 -dbPath " dynamo-directory)
         (provided (env :dynamodb-port "8000") => "7777")))

 (fact "Build dynamo command allows the in-memory? option to be specified in the project map"
       (let [project {:some-key "some-val"
                      :dynamodb-local {:in-memory? true}}]
         (build-dynamo-command project) => (str "java  -Djava.library.path=" dynamo-directory "/DynamoDBLocal_lib -jar " dynamo-directory "/DynamoDBLocal.jar -port 8000 -inMemory")))

 (fact "Build dynamo command does nothing if the in-memory? option is specified as false in the project map"
       (let [project {:some-key "some-val"
                      :dynamodb-local {:in-memory? false}}]
         (build-dynamo-command project) => (str "java  -Djava.library.path=" dynamo-directory "/DynamoDBLocal_lib -jar " dynamo-directory "/DynamoDBLocal.jar -port 8000 -dbPath " dynamo-directory)))

 (fact "Build dynamo command ignores the db-path option if in-memory? is specified in the project map"
       (let [project {:some-key "some-val"
                      :dynamodb-local {:db-path "some/path/that/will/be/ignored"
                                       :in-memory? true}}]
         (build-dynamo-command project) => (str "java  -Djava.library.path=" dynamo-directory "/DynamoDBLocal_lib -jar " dynamo-directory "/DynamoDBLocal.jar -port 8000 -inMemory")))

 (fact "Build dynamo command allows the shared-db? option to be specified in the project map"
       (let [project {:some-key "some-val"
                      :dynamodb-local {:shared-db? true}}]
         (build-dynamo-command project) => (str "java  -Djava.library.path=" dynamo-directory "/DynamoDBLocal_lib -jar " dynamo-directory "/DynamoDBLocal.jar -port 8000 -sharedDb -dbPath " dynamo-directory)))

 (fact "Build dynamo command does nothing if the shared-db? option is specified as false in the project map"
       (let [project {:some-key "some-val"
                      :dynamodb-local {:shared-db? false}}]
         (build-dynamo-command project) => (str "java  -Djava.library.path=" dynamo-directory "/DynamoDBLocal_lib -jar " dynamo-directory "/DynamoDBLocal.jar -port 8000 -dbPath " dynamo-directory)))

 (fact "Build dynamo command allows a db-path to specified"
       (let [project {:some-key "some-val"
                      :dynamodb-local {:db-path "some/path"}}]
         (build-dynamo-command project) => (str "java  -Djava.library.path=" dynamo-directory "/DynamoDBLocal_lib -jar " dynamo-directory "/DynamoDBLocal.jar -port 8000 -dbPath some/path")))

 (fact "Build dynamo command allows jvm-opts to be specified"
       (let [project {:some-key "some-val"
                      :dynamodb-local {:jvm-opts ["opt1" "opt2"]}}]
         (build-dynamo-command project) => (str "java opt1 opt2 -Djava.library.path=" dynamo-directory "/DynamoDBLocal_lib -jar " dynamo-directory "/DynamoDBLocal.jar -port 8000 -dbPath " dynamo-directory)))

 (fact "Ensure installed does not download DynamoDB Local if it already has been"
       (ensure-installed) => nil
       (provided
        (#'leiningen.dynamodb-local/exists? anything) => true))

 (fact "Ensure installed downloads and unpacks DynamoDB Local if it hasn't been already"
       (ensure-installed) => ...unpack-result...
       (provided
        (#'leiningen.dynamodb-local/exists? anything) => false
        (#'leiningen.dynamodb-local/download-dynamo anything) => ...download-result...
        (#'leiningen.dynamodb-local/unpack-dynamo) => ...unpack-result...)))
