(ns degasolv.pkgsys.subproc
  "Namespace containing functions related to the subprocess package system
   integration point."
  (:require [clojure.string :as string]
            [clojure.java.shell :as sh]
            [clojure.data.json :as json]
            [miner.tagged :as tag]
            [degasolv.util :refer :all]
            [tupelo.core :as t]
            [degasolv.resolver :as r :refer :all])
  (:import (java.util.zip GZIPInputStream)))

(defn make-slurper
  [{:keys [subproc-exe
           subproc-output-format]}]
  (fn slurp-subproc-repo [repo]
    (let [{:keys [exit out]}
          (sh/sh subproc-exe repo)]
      (when (not (= exit 0))
        (throw
         (ex-info (str
                   "Slurper `"
                   subproc-exe
                   "` given argument `"
                   repo
                   "` exited with non-zero status `"
                   exit
                   "`.")
                  {:slurper subproc-exe
                   :argument repo
                   :exit-status exit})))
      (let [packages
            (cond
              (= subproc-output-format "json")
              (json/read-str out :key-fn keyword)
              (= subproc-output-format "edn")
              (tag/read-string out)
              :else
              (throw (ex-info
                      (str "Unknown subproc output format: `"
                          subproc-output-format
                          "`")
                      {:subproc-output-format subproc-output-format})))
            repo-map
        (reduce
         (fn [c v]
           (update-in c [(:id v)] conj))
         {}
         packages)]
        (map-query repo-map)))))
