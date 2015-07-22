(ns govtrack-sync-app.neo4j.handlers
  (:require [clojurewerkz.neocons.rest.nodes :as nn]
            [clojurewerkz.neocons.rest.constraints :as nc]
            [clojurewerkz.neocons.rest.labels :as nl]
            [clojure.tools.logging :as log]
            [clojure.core.async :as async]))

(defn create-constraint-if-required [connection label property]
  (try
    (nc/get-unique connection label property)
    (catch Exception e
      (nc/create-unique connection label property))))

(defn handle-person-doc [connection channel]
  (async/go-loop []
    (let [doc (async/<!! channel)]
      (if-not (or (nil? doc) (contains? doc :drained))
        (do (nl/add connection (nn/create-unique-in-index connection :Person :thomas_id (:thomas_id doc) doc) :Person)
            (recur))
        (log/info (str "Finished Uploading People to Neo4J")))))
  (create-constraint-if-required connection :Person :thomas_id))