(ns govtrack-sync-app.govtrack.component
  (:require [com.stuartsierra.component :as comp]
           [clojure.core.async :as async]
           [clojure.tools.logging :as log]
           [govtrack-sync-app.govtrack.file-reader :as file-reader]))

(defrecord FileReaderComponent [directory file-pattern channels]
  comp/Lifecycle
  (start [component]
    (log/info (str "Starting file reading component for directory " directory))
    (file-reader/read-files directory, file-pattern (:channel channels)))
  (stop [component]
    (log/info (str "Stopping file reading component for directory " directory))
    (assoc component (:channel channels) nil)))

(defn new-file-reader [directory file-pattern]
  (map->FileReaderComponent {:directory directory :file-pattern file-pattern}))

(defrecord CsvReaderComponent [directory file-pattern channels]
  comp/Lifecycle
  (start [component]
    (log/info (str "Starting CSV reading component for directory " directory))
    (file-reader/read-csv directory file-pattern (:channel channels)))
  (stop [component]
    (log/info (str "Stopping CSV reading component for directory " directory))
    (assoc component (:channel channels) nil)))

(defn new-csv-reader [directory file-pattern]
  (map->CsvReaderComponent {:directory directory :file-pattern file-pattern}))