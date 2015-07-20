(ns govtrack-sync-app.channels.component
  (:require [com.stuartsierra.component :as comp]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]))

(defrecord CommunicationChannel [name channel-size]
  comp/Lifecycle
  (start [component]
    (log/info (str "Creating Channel "  name))
    (assoc component name (async/chan channel-size)))
  (stop [component]
    (log/info (str "Shutting down Channel " name))
    (assoc component name nil)))

(defn new-channel [name channel-size]
  (map->CommunicationChannel {:name name :channel-size channel-size}))
