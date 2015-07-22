(ns govtrack-sync-app.channels.component
  (:require [com.stuartsierra.component :as comp]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]))

(defrecord CommunicationChannel [name channel-size xform]
  comp/Lifecycle
  (start [component]
    (log/info (str "Creating Channel "  name))
    (if (nil? xform)
      (assoc component name (async/chan channel-size))
      (assoc component name (async/chan channel-size xform))))
  (stop [component]
    (log/info (str "Shutting down Channel " name))
    (assoc component name nil)))

(defn new-channel
  ([name channel-size] (map->CommunicationChannel {:name name :channel-size channel-size}))
  ([name channel-size xform] (map->CommunicationChannel {:name name :channel-size channel-size :xform xform})))
