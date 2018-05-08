(ns degasolv.pkgsys.core
  (:require
    [degasolv.util :refer :all]
    [clojure.spec :as s]
    [degasolv.resolver :as r :refer :all]
    [serovers.core :as vers]
    [clojure.java.io :as io]
    [clojure.string :as st]
    [miner.tagged :as tag]))

(defn- read-card!
  [card]
  (let [card-data (tag/read-string (default-slurp card))
        vetted-card-data
        (s/conform ::r/package card-data)]
    (if (= vetted-card-data
           ::s/invalid)
      (throw (ex-info (str
                        "Invalid data in card file `"
                        card
                        "`: "
                        (s/explain ::r/package
                                   card-data))
                      (s/explain-data ::r/package
                                      card-data)))
      card-data)))

(defn generate-repo-index!
  [search-directory
   index-file
   add-to
   version-comparator
   ]
  (let [output-file index-file
        initial-repository
        (if add-to
          (tag/read-string
            (default-slurp add-to))
          (hash-map))]
    (default-spit
      output-file
        (into (hash-map)
              (map
                (fn [x]
                  [(first x)
                   (into []
                         (sort #(- (version-comparator (:version %1)
                                                       (:version %2)))
                               (second x)))])
                (reduce
                  (fn merg [c v]
                    (update-in c [(:id v)] conj v))
                  initial-repository
                  (map
                    read-card!
                    (filter #(and (.isFile (io/file %))
                                  (= ".dscard" (st/replace % #"[^.]*[.]" "")))
                            (file-seq (io/file search-directory))))))))))

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
    [(memoize
      (map-query
       repo-data))]))
