(ns degasolv.pkgsys.apt
  "Namespace containing functions related to the APT package system."
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [degasolv.util :refer :all]
            [degasolv.resolver :as r :refer :all]
            [tupelo.core :as t])
  (:import (java.util.zip GZIPInputStream)))

; TODO: Add provides
;
; In case I change the zip input streamer later
(defn ->zip-input-stream
  [is]
  (GZIPInputStream. is))

(defn deb-to-degasolv-requirements
  [s]
  (if (empty? s)
    nil
    (t/it-> s
          (string/replace it #"[ ()]" "")
          (string/replace it #"<<" "<")
          (string/replace it #">>" ">")
          (string/replace it #"([^><=,]+)=([^><=|,]+)"
                          "$1==$2")
          (string/split it #",")
          (mapv
            #(string-to-requirement %)
            it))))

(defn start-pkg-segment?
  [lines]
  (t/truthy?
    (re-matches
      #"^Package:.*$"
      (first lines))))

(defn group-pkg-lines
  [lines]
  (t/partition-using
    start-pkg-segment?
    lines))

(defn lines-to-map
  [lines]
  (t/it-> lines
        (map
          (fn [line]
            (let [[_ k v] (re-matches #"^([^:]+): +(.*)$" line)]
              [(keyword
                (string/lower-case k))
              v]))
          it)
        (into {} it)))

(defn convert-pkg-requirements
  [pkg]
  (let [deps (:depends pkg)]
    (if deps
      (assoc
        pkg
        :depends
        (deb-to-degasolv-requirements
          deps))
      pkg)))

(defn add-pkg-location
  [pkg url]
  (assoc pkg
         :location
         (t/it-> url
               (str it "/" (:filename pkg))
               (string/replace
                 it
                 #"/+"
                 "/")
               (string/replace
                 it
                 #"^([a-zA-Z]+:)/"
                 "$1//"))))

(defn deb-to-degasolv-provides
  [s]
  (t/it-> s
        (string/replace it #"\p{Blank}" "")
        (string/split it #",")
        (into [] it)))

(defn expand-provides
  [pkg]
  (let [new-package
        (->PackageInfo
          (string/replace
            (:package pkg)
            #"[:]any$"
            "")
          (:version pkg)
          (:location pkg)
          (:depends pkg))]
  (if (:provides pkg)
    (t/it->
      (deb-to-degasolv-provides (:provides pkg))
        (map
        #(->PackageInfo
           %
           "0"
           (:location pkg)
           (:depends pkg))
        it)
      (conj
        it
        new-package))
    [new-package])))

(defn apt-repo
  [url info]
  (t/it-> info
        (string/split-lines it)
        (filter
          #(re-matches #"^(Provides|Version|Package|Depends|Filename):.*" %)
          it)
        (group-pkg-lines it)
        (map lines-to-map it)
        (map
          (fn each-package
            [pkg]
            (as->
              pkg each
              (convert-pkg-requirements each)
              (add-pkg-location each url)
              (expand-provides each)))
          it)
        (apply concat it)
        (reduce
          (fn [c v]
            (if (not (get c (:id v)))
              (assoc c (:id v) [v])
              (update-in
                c
                [(:id v)] conj v)))
          {}
          it)))

(defn slurp-apt-repo
  [repospec]
  (let [[pkgtype url dist & pools]
        (string/split repospec #" +")]
    (mapv
       (fn each-pool
         [pool]
         (map-query
           (t/it->
             pool
               (string/join
                 "/"
                 (if
                   (.contains pool "/")
                   [url
                    pool
                    "Packages.gz"]
                   [url
                    "dists"
                    dist
                    it
                    pkgtype
                    "Packages.gz"]))
               (with-open
                 [in
                  (->zip-input-stream
                    (io/input-stream it))]
                 (slurp in))
               (apt-repo url it))))
       pools)))
