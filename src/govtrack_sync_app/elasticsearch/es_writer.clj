(ns govtrack-sync-app.elasticsearch.es-writer
  (:require [clojure.core.async :as async]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.rest.bulk :as esrb]
            [clojure.tools.logging :as log]))


(defn drained? [map]
  (if (contains? map :drained)
    true
    false))

(defn batch [channel batch-size]
  (let [timeout-chan (async/timeout 500)
        batch (->> (range batch-size)
                   (map (fn [_]
                          (let [result (first (async/alts!! [channel timeout-chan] :priority true))]
                            result)))
                   (remove (comp nil?)))]
      (if (drained? (last batch))
        [(filter #(not (contains? % :drained)) batch) true]
        [batch false])))

(defn write-to-es [connection index type channel]
  (async/go-loop []
    (let [[batch drained?] (batch channel 500)]
      (if-not (empty? batch)
        (esrb/bulk-with-index-and-type connection index (str type) (esrb/bulk-index batch)))
        (log/info (str (count batch) " Documents written to ElasticSearch"))
      (if (false? drained?)
        (recur)
        (log/info (str "Finished writing " type  " documents to elasticsearch"))))))