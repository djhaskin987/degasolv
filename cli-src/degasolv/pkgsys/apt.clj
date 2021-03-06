(ns degasolv.pkgsys.apt
  "Namespace containing functions related to the APT package system."
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [degasolv.util :refer :all]
            [degasolv.resolver :as r :refer :all]
            [serovers.core :as vers])
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
    (as-> s it
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

(defn lines-to-map
  [lines]
  (as-> lines it
        (map
          (fn [line]
            (let [[_ k v] (re-matches #"^([^:]+): +(.*)$" line)]
              [(keyword
                (string/lower-case k))
              v]))
          it)
        (transient (into (hash-map) it))))

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
         (as-> url it
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
  (as-> s it
        (string/replace it #"\p{Blank}" "")
        (string/split it #",")
        (into [] it)))

(defn expand-provides
  [pkg]
  (let [restof-package
        (dissoc
          pkg
          :version
          :location
          :depends)
        new-package
        (into
         (->PackageInfo
          (string/replace
           (:package pkg)
           #"[:]any$"
           "")
          (:version pkg)
          (:location pkg)
          (:depends pkg))
         restof-package)]
  (if (:provides pkg)
    (as->
      (deb-to-degasolv-provides (:provides pkg)) it
        (map
         #(into (->PackageInfo
                 %
                 "0"
                 (:location pkg)
                 (:depends pkg))
                restof-package)
        it)
      (conj
        it
        new-package))
    [new-package])))

(defn apt-repo
  [url info]
  (as->
    info it
    (string/split it #"\n\n")
    (map
      (fn each-package
        [pkg]
        (as->
          pkg each
          (string/split-lines each)
          (filter
            #(re-matches #"^(\p{Alnum}+):.*" %)
            each)
          (lines-to-map each)
          (convert-pkg-requirements each)
          (add-pkg-location each url)
          (expand-provides (persistent! each))))
      it)
    (apply concat it)
    ;; (fn query [id]
    ;;   (sort-by
    ;;     :version
    ;;     #(- (vers/debian-vercmp %1 %2))
    ;;     (filter
    ;;     #(= id (:id %))
    ;;     it)))
    ;; (memoize it)))
   (reduce
     (fn conjv
       [c v]
       (update-in c
                  [(:id v)]
                  #(conj (vec %1) %2)
                  v))
     (hash-map)
     it)
   (map-query it)
   (memoize it)))

(defn slurp-apt-repo
  [repospec]
  (let [[pkgtype url dist & pools]
        (string/split repospec #" +")]
     (mapv
      (fn each-loc
           [loc]
           (as-> loc it
            (string/join "/" it)
            (let
              [in
               (->zip-input-stream
                (io/input-stream it))]
              (try (slurp in)
                (finally (.close ^java.io.InputStream in))))
            (apt-repo url it)))
           (if (.contains ^java.lang.String dist "/")
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
