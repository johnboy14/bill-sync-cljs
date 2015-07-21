(ns govtrack-sync-app.transformers.transformers
  (:require [cheshire.core :as ch]))

(def bill-transformer
  (comp
    (map #(ch/parse-string % true))
    (map #(assoc % :_id (:bill_id %)))))

(def people-transformer
  (comp
    (map #(if (empty? (:thomas_id %))
           %
           (assoc % :_id (:thomas_id %))))))
