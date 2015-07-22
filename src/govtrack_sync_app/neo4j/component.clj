(ns govtrack-sync-app.neo4j.component
  (:require [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as comp]
            [clojurewerkz.neocons.rest :as nr]
            [clojurewerkz.neocons.rest.nodes :as nn]))

(defn read [connection channel]
  (async/go-loop []
    (let [v (async/<!! channel)]
      (if-not (or (nil? v) (contains? v :drained))
        (do (nn/create connection v)
            (recur))
        (println "Done")))))

(defrecord Neo4JClientComponent [channels]
  comp/Lifecycle
  (start [component]
    (log/info "Starting Neo4J Client Component")
    (let [connection (nr/connect "http://localhost:7474/db/data/" "neo4j" "password")]
      (println connection)
      (read connection (:channel channels))
      (assoc component :connection connection)))
  (stop [component]
    (log/info "Stopping Neo4J Client Component")
    (assoc component :connection nil)))

(defn new-neo-client []
  (map->Neo4JClientComponent {}))