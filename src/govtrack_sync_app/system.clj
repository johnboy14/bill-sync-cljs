(ns govtrack-sync-app.system
  (:require [com.stuartsierra.component :as component]
            [govtrack-sync-app.channels.channel-maker :as chan-maker]
            [govtrack-sync-app.govtrack.file-reader-comp :as reader-comp]
            [govtrack-sync-app.elasticsearch.elasticsearch :as es-client]
            [govtrack-sync-app.switchboard.switchboard :as switchboard]))



(defn new-system []
  (component/system-map 
   :bill-file-chan (chan-maker/new-channel :channel 100)
   :bill-es-chan (chan-maker/new-channel :channel 100)
   :bill-file-reader (component/using (reader-comp/new-file-reader "test-resources/bills" #".*data.json")
                                      {:channels :bill-file-chan})
   :bill-elastic-client (component/using (es-client/new-elasticsearch-client "congress" "bill")
                                         {:channels :bill-es-chan})
   :switchboard (component/using (switchboard/new-switchboard)
                                 {:bill-file-chan :bill-file-chan :bill-es-chan :bill-es-chan})))
