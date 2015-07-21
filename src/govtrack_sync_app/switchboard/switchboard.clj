(ns govtrack-sync-app.switchboard.switchboard
  (:require [com.stuartsierra.component :as comp]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [cheshire.core :as ch]))

(def bill-transformer
  (comp
    (map #(ch/parse-string % true))
    (map #(assoc % :_id (:bill_id %)))))

(def people-transformer
  (comp
    (map #(if (empty? (:thomas_id %))
           %
           (assoc % :_id (:thomas_id %))))))

(defn pipe-channels [chan1 transformer chan2]
  (async/pipeline 1 chan2 transformer chan1))

(defrecord SwitchBoardComponent [bill-file-chan bill-es-chan
                                 people-csv-chan people-es-chan]
  comp/Lifecycle
  (start [component]
    (log/info (str "Starting Switchboard compoent"))
    (pipe-channels (:channel bill-file-chan) bill-transformer (:channel bill-es-chan))
    (pipe-channels (:channel people-csv-chan) people-transformer (:channel people-es-chan)))
  (stop [component]
    (log/info (str "Stopping Switchboard compoent"))))

(defn new-switchboard []
  (map->SwitchBoardComponent {}))
