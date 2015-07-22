(ns govtrack-sync-app.neo4j.handlers
  (:require [clojurewerkz.neocons.rest.nodes :as nn]
            [clojurewerkz.neocons.rest.constraints :as nc]
            [clojurewerkz.neocons.rest.cypher :as cy]
            [clojurewerkz.neocons.rest.labels :as nl]))

(defn create-constraint-if-required [connection label property]
  (if (nil? (nc/get-unique connection label property))
    (nc/create-unique connection label property)))

(defn handle-person-doc [connection doc]
  (create-constraint-if-required connection :Person :thomas_id)
  (nl/add connection (nn/create-unique-in-index connection :Person :thomas_id (:thomas_id doc) doc) :Person))