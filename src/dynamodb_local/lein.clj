(ns dynamodb-local.lein
  (:require [dynamodb-local.core :as core]
            [leiningen.core.main :as main]))

(defn dynamodb-local
  "Run DynamoDB Local for the lifetime of the given task."
  [project & args]
  (core/ensure-installed main/info)
  (let [dynamo-process (core/start-dynamo project)]
    (main/info "dynamodb-local: Started DynamoDB Local")
    (core/handle-shutdown main/info dynamo-process)
    (if (seq args)
      (main/apply-task (first args) project (rest args))
      (while true (Thread/sleep 5000)))))
