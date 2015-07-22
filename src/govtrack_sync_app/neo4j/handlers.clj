(ns govtrack-sync-app.neo4j.handlers
  (:require [clojurewerkz.neocons.rest.nodes :as nn]
            [clojurewerkz.neocons.rest.constraints :as nc]
            [clojurewerkz.neocons.rest.labels :as nl]
            [clojure.tools.logging :as log]
            [clojure.core.async :as async]
            [govtrack-sync-app.utils.chan-utils :as chan-utils]))

(defn create-constraint-if-required [connection label property]
  (try
    (nc/get-unique connection label property)
    (catch Exception e
      (nc/create-unique connection label property))))

(defn handle-person-doc [connection channel]
  (async/go-loop []
    (let [[batch drained?] (chan-utils/batch channel 500)]
      (if-not (empty? batch)
        (doseq [doc batch]
          (nl/add connection (nn/create-unique-in-index connection :Person :thomas_id (:thomas_id doc) doc) :Person))
        (log/info (str (count batch) " Documents written to Neo4J.")))
      (if (false? drained?)
        (recur)
        (log/info (str "Finished Uploading People to Neo4J")))))
  (create-constraint-if-required connection :Person :thomas_id))