(ns degasolv.pkgsys.subproc
  "Namespace containing functions related to the subprocess package system
   integration point."
  (:require [clojure.string :as string]
            [clojure.java.shell :as sh]
            [clojure.data.json :as json]
            [clojure.walk :as walk]
            [miner.tagged :as tag]
            [degasolv.util :refer :all]
            [degasolv.resolver :as r :refer :all])
  (:import (java.util.zip GZIPInputStream)))

(defn convert-input [raw-repo-info]
  (as-> raw-repo-info it
    (seq it)
    (map (fn [[package-name package-list]]
           [package-name
            (mapv (fn [pkg]
                    (as-> pkg each-pkg
                      (walk/keywordize-keys each-pkg)
                      (if (:requirements each-pkg)
                        (assoc
                         each-pkg
                         :requirements
                         (mapv string-to-requirement (:requirements each-pkg)))
                        each-pkg)
                      (into
                       (->PackageInfo
                        (:id each-pkg)
                        (:version each-pkg)
                        (:location each-pkg)
                        (:requirements each-pkg))
                       (dissoc each-pkg
                               :id
                               :version
                               :location
                               :requirements))))
                  package-list)])
         it)
    (into (hash-map) it)))

(defn make-slurper
  [{:keys [subproc-exe
           subproc-output-format]}]
  (fn slurp-subproc-repo [repo]
    (let [{:keys [exit out]}
          (sh/sh subproc-exe repo)]
      (when (not (= exit 0))
        (throw
         (ex-info (str
                   "Executable `"
                   subproc-exe
                   "` given argument `"
                   repo
                   "` exited with non-zero status `"
                   exit
                   "`.")
                  {:slurper subproc-exe
                   :argument repo
                   :exit-status exit})))
      (let [raw-repo-info
            (cond
              (= subproc-output-format "json")
              (json/read-str out)
              (= subproc-output-format "edn")
              (tag/read-string out)
              :else
              (throw (ex-info
                      (str "Unknown subproc output format: `"
                          subproc-output-format
                          "`")
                      {:subproc-output-format subproc-output-format})))
            repo-map
            (convert-input raw-repo-info)]
        (map-query repo-map)))))
