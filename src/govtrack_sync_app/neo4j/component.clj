(ns govtrack-sync-app.neo4j.component
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as comp]
            [clojurewerkz.neocons.rest :as nr]))


(defrecord Neo4JClientComponent [channels handler-fn]
  comp/Lifecycle
  (start [component]
    (log/info "Starting Neo4J Client Component")
    (let [connection (nr/connect "http://localhost:7474/db/data/" "neo4j" "password")]
      (println connection)
      (handler-fn connection (:channel channels))
      (assoc component :connection connection)))
  (stop [component]
    (log/info "Stopping Neo4J Client Component")
    (assoc component :connection nil)))

(defn new-neo-client [handler-fn]
  (map->Neo4JClientComponent {:handler-fn handler-fn}))