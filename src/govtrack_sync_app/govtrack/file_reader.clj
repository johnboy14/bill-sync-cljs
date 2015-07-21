(ns govtrack-sync-app.govtrack.file-reader
  (:require [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]))

(defn walk [dirpath pattern]
  (doall (filter #(re-matches pattern (.getName %))
                 (file-seq (io/file dirpath)))))

(defn read-files [dirpath pattern channel]
  (log/info (str "Start reading directory " dirpath))
  (async/go
    (doseq [file (walk dirpath #".*data.json")]
      (async/>!! channel (slurp file)))
    (async/>!! channel "{\"drained\":\"true\"}")
    (log/info (str "Finished reading directory " dirpath))))
