(ns govtrack-sync-app.neo4j.handlers
  (:require [clojurewerkz.neocons.rest.nodes :as nn]
            [clojurewerkz.neocons.rest.relationships :as nr]
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

(defn retrieve-bill-details [bill]
  (-> bill
      (dissoc :actions)
      (dissoc :amendments)
      (dissoc :committees)
      (dissoc :cosponsors)
      (dissoc :related_bills)
      (dissoc :history)
      (dissoc :sponsor)
      (dissoc :subjects)
      (dissoc :summary)
      (dissoc :titles)
      (dissoc :enacted_as)
      ((fn [m] (into {} (remove (fn [[_ v]] (nil? v)) m))))))

(defn handle-person-doc [connection channel]
  (async/go-loop []
    (let [[batch drained?] (chan-utils/batch channel 500)]
      (if-not (empty? batch)
        (doseq [doc batch]
          (nl/add connection (nn/create-unique-in-index connection "Person" "thomas_id" (:thomas_id doc) doc) "Person"))
        (log/info (str (count batch) " Documents written to Neo4J.")))
      (if (false? drained?)
        (recur)
        (log/info (str "Finished Uploading People to Neo4J")))))
      (create-constraint-if-required connection :Person :thomas_id)
  )

(defn handle-bill-doc [connection channel]
  (Thread/sleep 10000)
  (async/go-loop []
    (let [[batch drained?] (chan-utils/batch channel 500)]
      (if-not (empty? batch)
        (doseq [doc batch]
          (let [node (nn/create-unique-in-index connection "Bill" "bill_id" (:bill_id doc) (retrieve-bill-details doc))
                sponsor (nn/find-one connection "Person" "thomas_id" (:thomas_id (:sponsor doc)))]
            (if-not (nil? sponsor)
              (do (nr/create-unique-in-index connection sponsor node :sponsoring "sponsoring" "thomas_id" {:thomas_id (:thomas_id (:sponsor doc))})
                  (nr/create-unique-in-index connection node sponsor :sponsoredby "sponsoredby" "thomas_id" {:thomas_id (:thomas_id (:sponsor doc))})))
            (nl/add connection node :Bill)))
        (log/info (str (count batch) " Documents written to Neo4J.")))
      (if (false? drained?)
        (recur)
        (log/info (str "Finished Uploading Bills to Neo4J")))))
  (create-constraint-if-required connection :Bill :bill_id))