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
          (string/replace it #":(any|i386|amd64)" "")
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
        (transient (into {} it))))

(defn convert-pkg-requirements
  [pkg]
  (let [deps (:depends pkg)]
    (if deps
      (assoc!
        pkg
        :depends
        (deb-to-degasolv-requirements
          deps))
      pkg)))

(defn add-pkg-location
  [pkg url]
  (assoc! pkg
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
  (t/it->
    info
    (string/split it #"\n\n")
    (map
      (fn each-package
        [pkg]
        (as->
          pkg each
          (string/split-lines each)
          (filter
            #(re-matches #"^(Provides|Version|Package|Depends|Filename):.*" %)
            each)
          (lines-to-map each)
          (convert-pkg-requirements each)
          (add-pkg-location each url)
          (expand-provides each)))
      it)
    (apply concat it)
    (fn query [id]
      (filter
        #(= id (:id %))
        it))))
;;    (reduce
;;      (fn conjv
;;        [c v]
;;        (update-in c
;;                   [(:id v)]
;;                   #(conj (vec %1) %2)
;;                   v))
;;      {}
;;      it)
;;  (map-query it)))

(defn slurp-apt-repo
  [repospec]
  (let [[pkgtype url dist & pools]
        (string/split repospec #" +")]
     (mapv
      (fn each-loc
           [loc]
           (t/it->
            loc
            (string/join "/" it)
            (with-open
              [in
               (->zip-input-stream
                (io/input-stream it))]
              (slurp in))
            (apt-repo url it)))
           (if (.contains dist "/")
             [[url
               dist
               "Packages.gz"]]
             (mapv
              (fn each-pool
                [pool]
                [url
                 "dists"
                 dist
                 pool
                 pkgtype
                 "Packages.gz"])
              pools)))))
