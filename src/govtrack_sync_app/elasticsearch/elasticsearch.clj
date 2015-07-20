(ns govtrack-sync-app.elasticsearch.elasticsearch
  (:require [com.stuartsierra.component :as comp]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [clojurewerkz.elastisch.rest :as esr]
            [govtrack-sync-app.elasticsearch.es-writer :as writer]))

(defrecord ElasticSearchClient [index type channels]
  comp/Lifecycle
  (start [component]
    (log/info (str "Starting ElasticSearchClient record type " type))
    (let [connection (esr/connect)]
      (writer/write-to-es connection index type (:channel channels))
      (assoc component (keyword (str type "-connection")) connection)))
  (stop [component]
    (log/info (str "Stopping ElasticSearchClient record type " type))
    (assoc component (keyword (str type "-connection")) nil)))

(defn new-elasticsearch-client [index type]
  (map->ElasticSearchClient {:index index :type type}))



