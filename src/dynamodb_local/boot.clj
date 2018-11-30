(ns dynamodb-local.boot
  {:boot/export-tasks true}
  (:require [boot.core :as boot]
            [boot.util :as util]
            [dynamodb-local.core :as core]))

(defn- info
  [& message]
  (apply util/info message)
  (util/info "\n"))

(boot/deftask dynamodb-local
  [p project CONFIG edn "The configuration of the dynamo db process"]
  (boot/with-pass-thru _
    (core/ensure-installed info)
    (let [dynamo-process (core/start-dynamo project)]
      (info "dynamodb-local: Started DynamoDB Local")
      (core/handle-shutdown info dynamo-process))))
