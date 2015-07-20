(ns govtrack-sync-app.elasticsearch.es-writer
  (:require [clojure.core.async :as async]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.rest.bulk :as esrb]
            [clojure.tools.logging :as log]))


(defn lazy-contains? [coll key]
  (boolean (some #(= % key) coll)))

(defn batch [channel batch-size]
  (let [timeout-chan (async/timeout 500)
        batch (->> (range batch-size)
                   (map (fn [_]
                          (let [val (async/alts!! [channel timeout-chan] :priority true)
                                result (first val)
                                port (second val)]
                            (if (and (nil? result) (= port channel))
                              :drained
                              result))))
                   (remove (comp nil?)))]
      (if (lazy-contains? batch :drained)
        [(filter #(not (keyword? %)) batch) true]
        [batch false])))

(defn write-to-es [connection index type channel]
  (async/go-loop []
    (let [[batch drained?] (batch channel 100)]
      (if-not (empty? batch)
        (esrb/bulk-with-index-and-type connection index (str type) (esrb/bulk-index batch)))
        (log/info (str (count batch) " Documents written to ElasticSearch"))
      (if (false? drained?)
        (recur)
        (log/info (str "Finished writing " type  " documents to elasticsearch"))))))