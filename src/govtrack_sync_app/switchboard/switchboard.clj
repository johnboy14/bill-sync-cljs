(ns govtrack-sync-app.switchboard.switchboard
  (:require [com.stuartsierra.component :as comp]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [govtrack-sync-app.transformers.transformers :as t]))

(defn pipe-channels [chan1 transformer chan2]
  (async/pipeline 1 chan2 transformer chan1))

(defrecord SwitchBoardComponent [bill-file-chan bill-es-chan
                                 people-csv-chan people-es-chan]
  comp/Lifecycle
  (start [component]
    (log/info (str "Starting Switchboard compoent"))
    (pipe-channels (:channel bill-file-chan) t/bill-transformer (:channel bill-es-chan))
    (pipe-channels (:channel people-csv-chan) t/people-transformer (:channel people-es-chan)))
  (stop [component]
    (log/info (str "Stopping Switchboard compoent"))))

(defn new-switchboard []
  (map->SwitchBoardComponent {}))
