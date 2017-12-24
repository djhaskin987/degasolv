(ns degasolv.pkgsys.subproc
  "Namespace containing functions related to the subprocess package system
   integration point."
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [degasolv.util :refer :all]
            [degasolv.resolver :as r :refer :all]
            [tupelo.core :as t]
            [serovers.core :as vers])
  (:import (java.util.zip GZIPInputStream)))

(defn make-slurper
  [{:keys [subproc-exe
           subproc-out-format]}]
  (fn [repo] slurp-subproc-repo
    (let [{:keys [exit out]}
          (sh/sh [subproc-exe repo])]
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
              (= subproc-out-format "json")
              (json/read-str out :key-fn keyword)
              (= subproc-out-format "edn")
              (tag/read-string out)
              :else
              (throw (ex-info
                      (str "Unknown subproc output format: `"
                          subproc-out-format
                          "`")
                      {:subproc-out-format subproc-out-format})))
            repo-map
        (reduce
         (fn [c v]
           (update-in c [(:id v)] conj))
         {}
         packages)]
        (map-query repo-map)))))
