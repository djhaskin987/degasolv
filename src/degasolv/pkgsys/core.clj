(ns degasolv.pkgsys.core
  (:require
    [degasolv.util :refer :all]
    [clojure.spec :as s]
    [degasolv.resolver :as r :refer :all]
    [miner.tagged :as tag]))

(defn slurp-degasolv-repo
  [url]
  (let
      [repo-data
       (tag/read-string
        (default-slurp url))
       vetted-repo-data
       (s/conform
        ::r/map-repo
        repo-data)]
    (when (= ::s/invalid vetted-repo-data)
      (throw (ex-info
              (str
               "Invalid requirement string in repo `"
               url
               "`: "
               (s/explain ::r/map-repo repo-data))
              (s/explain-data ::r/map-repo
                              repo-data))))
    [repo-data]))
