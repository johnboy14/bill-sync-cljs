(ns govtrack-sync-app.system
  (:require [com.stuartsierra.component :as component]
            [govtrack-sync-app.channels.component :as chan-maker]
            [govtrack-sync-app.govtrack.component :as reader-comp]
            [govtrack-sync-app.elasticsearch.component :as es-client]
            [govtrack-sync-app.switchboard.switchboard :as switchboard]))



(defn new-system []
  (component/system-map 
   :bill-file-chan (chan-maker/new-channel :channel 100)
   :bill-es-chan (chan-maker/new-channel :channel 100)
   :bill-file-reader (component/using (reader-comp/new-file-reader "/Users/johnervine/govtrack.us-data/data/bills" #".*data.json")
                                      {:channels :bill-file-chan})
   :bill-elastic-client (component/using (es-client/new-elasticsearch-client "congress" "bill")
                                         {:channels :bill-es-chan})
   :switchboard (component/using (switchboard/new-switchboard)
                                 {:bill-file-chan :bill-file-chan :bill-es-chan :bill-es-chan})))
