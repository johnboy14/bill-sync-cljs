(ns govtrack-sync-app.switchboard.switchboard
  (:require [com.stuartsierra.component :as comp]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [cheshire.core :as ch]))

(defn pipe-channels [chan1 chan2]
  (async/pipe chan1 chan2))

(def xform
  (comp
    (map #(ch/parse-string % true))
    (map #(assoc % :_id (:bill_id %)))))

(defn pipe-channels [chan1 chan2]
  (async/pipeline 1 chan2 xform chan1))

(defrecord SwitchBoardComponent [bill-file-chan bill-es-chan]
  comp/Lifecycle
  (start [component]
    (log/info (str "Starting Switchboard compoent"))
    (pipe-channels (:channel bill-file-chan) (:channel bill-es-chan)))
  (stop [component]
    (log/info (str "Stopping Switchboard compoent"))))

(defn new-switchboard []
  (map->SwitchBoardComponent {}))
