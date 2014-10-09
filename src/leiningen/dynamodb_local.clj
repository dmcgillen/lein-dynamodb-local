(ns leiningen.dynamodb-local
  (:require [clojure.java.io :as io]
            [leiningen.core.main :as main])
  (:import [java.nio.file Files]
           [java.nio.file.attribute FileAttribute]
           [org.apache.commons.io FileUtils]
           [net.lingala.zip4j.core ZipFile]))

(def ^:private dynamodb-local-version "dynamodb_local_2014-04-24")

(def ^:private temp-directory
  (delay (-> (Files/createTempDirectory "lein-dynamodb-local" (make-array FileAttribute 0))
             (.toString))))

(defn- start-dynamo
  "Start DynamoDB Local with the given options"
  [dir port in-memory? db-path]
  (let [lib-path (str (io/file dir dynamodb-local-version "DynamoDBLocal_lib"))
        jar-path (str (io/file dir dynamodb-local-version "DynamoDBLocal.jar"))
        command (cond-> (format "java -Djava.library.path=%s -jar %s -port %s" lib-path jar-path port)
                        in-memory? (str " -inMemory")
                        (and (seq db-path) (not in-memory?)) (str " -dbPath " db-path))]
    (.exec (Runtime/getRuntime) command)))

(defn- dynamo-options
  "Use DynamoDB Local options provided or default values"
  [project]
  (merge {:port 8000
          :in-memory? false
          :db-path "."}
         (:dynamodb-local project)))

(defn- unpack-dynamo
  "Unpack the DynamoDB Local jar and libraries to the specified directory"
  [dir]
  (let [zip-file (str dynamodb-local-version ".zip")
        temp-zip (io/file dir zip-file)]
    (with-open [zip-stream (.openStream (io/resource zip-file))]
      (io/copy zip-stream temp-zip))
    (.extractAll (ZipFile. temp-zip) dir)))

(defn- delete-directory-on-shutdown
  "Delete a directory when the JVM shuts down"
  [dir]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn [] (FileUtils/deleteDirectory (io/file dir))))))

(defn dynamodb-local
  "Run a local DynamoDB for the lifetime of the given task"
  [project & args]
  (delete-directory-on-shutdown @temp-directory)
  (unpack-dynamo @temp-directory)
  (let [{:keys [port in-memory? db-path]} (dynamo-options project)
        dynamo-process (start-dynamo @temp-directory port in-memory? db-path)]
    (if (seq args)
      (try (main/apply-task (first args) project (rest args))
           (finally (.destroy dynamo-process)))
      (while true (Thread/sleep 5000)))))
