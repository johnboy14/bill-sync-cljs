(ns govtrack-sync-app.elasticsearch.es-writer
  (:require [clojure.core.async :as async]
            [clojurewerkz.elastisch.rest.bulk :as esrb]
            [govtrack-sync-app.utils.chan-utils :as chan-utils]
            [clojure.tools.logging :as log]))

(defn write-to-es [connection index type channel]
  (async/go-loop []
    (let [[batch drained?] (chan-utils/batch channel 500)]
      (if-not (empty? batch)
        (esrb/bulk-with-index-and-type connection index (str type) (esrb/bulk-index batch)))
        (log/info (str (count batch) " Documents written to ElasticSearch"))
      (if (false? drained?)
        (recur)
        (log/info (str "Finished writing " type  " documents to elasticsearch"))))))