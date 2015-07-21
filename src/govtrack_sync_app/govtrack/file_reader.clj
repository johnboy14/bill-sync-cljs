(ns govtrack-sync-app.govtrack.file-reader
  (:require [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.walk :as w]
            [csv-map.core :as csv]))

(def drained-message "{\"drained\":\"true\"}")

(defn walk [dirpath pattern]
  (doall (filter #(re-matches pattern (.getName %))
                 (file-seq (io/file dirpath)))))

(defn read-files [dirpath pattern channel]
  (log/info (str "Start reading directory " dirpath))
  (async/go
    (doseq [file (walk dirpath (re-pattern pattern))]
      (async/>!! channel (slurp file)))
    (async/>!! channel drained-message)
    (log/info (str "Finished reading directory " dirpath))))

(defn read-csv [dirpath pattern channel]
  (log/info (str "Start reading directory " dirpath))
  (async/go
    (doseq [csv-file (walk dirpath (re-pattern pattern))]
      (doseq [contents (-> (slurp csv-file) (csv/parse-csv) (w/keywordize-keys))]
        (async/>!! channel contents)))
    (async/>!! channel {:drained true})
    (log/info (str "Finished reading csv directory " dirpath))))
