(ns govtrack-sync-app.switchboard.switchboard
  (:require [com.stuartsierra.component :as comp]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [govtrack-sync-app.transformers.transformers :as t]))

(defn pipe-channels [chan1 transformer chan2]
  (async/pipeline 1 chan2 transformer chan1))

(defn tap-channels [tapFrom tap1 tap2]
  (let [mult (async/mult tapFrom)]
    (async/tap mult tap1)
    (async/tap mult tap2)))

(defrecord SwitchBoardComponent [bill-file-chan bill-es-chan bill-neo-chan
                                 people-csv-chan people-es-chan people-neo-chan]
  comp/Lifecycle
  (start [component]
    (log/info (str "Starting Switchboard compoent"))
    (tap-channels (:channel bill-file-chan) (:channel bill-es-chan) (:channel bill-neo-chan))
    (tap-channels (:channel people-csv-chan) (:channel people-es-chan) (:channel people-neo-chan)))

  (stop [component]
    (log/info (str "Stopping Switchboard compoent"))))

(defn new-switchboard []
  (map->SwitchBoardComponent {}))
