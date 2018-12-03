(ns dynamodb-local.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [environ.core :refer [env]])
  (:import [java.io File]
           [java.nio.file Files Paths LinkOption Path]
           [java.nio.file.attribute FileAttribute]
           [net.lingala.zip4j.core ZipFile]))

(def ^:private download-url "https://s3-us-west-2.amazonaws.com/dynamodb-local/dynamodb_local_latest.zip")

(def ^:private dynamo-directory (str (System/getProperty "user.home") File/separator ".lein-dynamodb-local"))

(defn- ->path
  "Create a path from the given strings."
  [str & strs]
  {:pre [(string? str) (every? string? strs)]}
  (Paths/get str (into-array String strs)))

(defn- path?
  "Is the given argument a path?"
  [x]
  (instance? Path x))

(defn- exists?
  "Does the given path exist?"
  [path]
  {:pre [(path? path)]}
  (Files/exists path (into-array LinkOption [])))

(defn- ensure-dynamo-directory
  "Make sure the directory that DynamoDB Local will be downloaded to
  exists."
  []
  (let [path (->path dynamo-directory)]
    (when-not (exists? path)
      (-> (Files/createDirectory path (make-array FileAttribute 0))
          (.toString)))))

(defn- dynamo-options
  "Use DynamoDB Local options provided or default values."
  [project]
  (merge {:port (Integer/valueOf (env :dynamodb-port "8000"))
          :in-memory? false
          :db-path dynamo-directory}
         (:dynamodb-local project)))

(defn- build-dynamo-command
  "Build a java command to start DynamoDB Local with the required
  options."
  [project]
  (let [{:keys [port in-memory? shared-db? db-path jvm-opts]} (dynamo-options project)
        lib-path (str (io/file dynamo-directory "DynamoDBLocal_lib"))
        jar-path (str (io/file dynamo-directory "DynamoDBLocal.jar"))]
    (cond-> (format "java %s -Djava.library.path=%s -jar %s -port %s" (str/join " " jvm-opts) lib-path jar-path port)
      in-memory? (str " -inMemory")
      shared-db? (str " -sharedDb")
      (and (seq db-path) (not in-memory?)) (str " -dbPath " db-path))))

(defn start-dynamo
  "Start DynamoDB Local with the desired options."
  [log project]
  (let [dynamo-process (->> (build-dynamo-command project)
                            (.exec (Runtime/getRuntime)))]
    (log "dynamodb-local: Started DynamoDB Local" project)
    dynamo-process))

(defn- download-dynamo
  "Download DynamoDB Local from Amazon."
  [log url]
  (log "dynamodb-local: Downloading DynamoDB Local to" dynamo-directory)
  (ensure-dynamo-directory)
  (io/copy (io/input-stream (io/as-url url)) (io/as-file (str dynamo-directory "/" "dynamo.zip"))))

(defn- unpack-dynamo
  "Unzip a DynamoDB Local download."
  [log]
  (log "dynamodb-local: Unpacking DynamoDB Local")
  (let [zip-file (->path dynamo-directory "dynamo.zip")]
    (.extractAll (ZipFile. (str zip-file)) dynamo-directory)))

(defn ensure-installed
  "Download and unpack DynamoDB Local if it hasn't been already."
  [log]
  (when-not (exists? (->path dynamo-directory "dynamo.zip"))
    (download-dynamo log download-url)
    (unpack-dynamo log)))

(defn handle-shutdown
  "Kill the DynamoDB Local process on JVM shutdown."
  [log dynamo-process]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn []
                               (doto dynamo-process (.destroy) (.waitFor))
                               (log "dynamodb-local: Exited with code:" (.exitValue dynamo-process))))))
