(ns govtrack-sync-app.system
  (:require [com.stuartsierra.component :as component]
            [govtrack-sync-app.channels.component :as chan-maker]
            [govtrack-sync-app.govtrack.component :as reader-comp]
            [govtrack-sync-app.elasticsearch.component :as es-client]
            [govtrack-sync-app.neo4j.component :as neo-client]
            [govtrack-sync-app.switchboard.switchboard :as switchboard]
            [govtrack-sync-app.transformers.transformers :as t]))



(defn new-system []
  (component/system-map 
   :bill-file-chan (chan-maker/new-channel :channel 100)
   :bill-es-chan (chan-maker/new-channel :channel 100)
   :bill-file-reader (component/using (reader-comp/new-file-reader "test-resources/bills" ".*data.json")
                                      {:channels :bill-file-chan})
   :bill-elastic-client (component/using (es-client/new-elasticsearch-client "congress" "bill")
                                         {:channels :bill-es-chan})

   :people-csv-chan (chan-maker/new-channel :channel 100 t/people-transformer)
   :people-es-chan (chan-maker/new-channel :channel 100)
   :people-neo-chan (chan-maker/new-channel :channel 100)
   :people-csv-reader (component/using (reader-comp/new-csv-reader "test-resources/legislators" ".*csv")
                                       {:channels :people-csv-chan})
   :people-elastic-client (component/using (es-client/new-elasticsearch-client "congress" "person")
                                           {:channels :people-es-chan})
   :people-neo-client (component/using (neo-client/new-neo-client)
                                       {:channels :people-neo-chan})

   :switchboard (component/using (switchboard/new-switchboard)
                                 {:bill-file-chan :bill-file-chan :bill-es-chan :bill-es-chan
                                  :people-csv-chan :people-csv-chan :people-es-chan :people-es-chan
                                  :people-neo-chan :people-neo-chan})))
