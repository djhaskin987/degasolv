(ns degasolv.util
  (:require [clojure.java.io :as io]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]))

; Deprecated, we should just use update-in
(defn assoc-conj
  [mp k v]
  (update-in mp [k] conj v))

; UTF-8 by default :)
(defn default-slurp [loc]
  (let [input (if (= loc "-")
                    *in*
                    loc)]
    (clojure.core/slurp input :encoding "UTF-8")))

(defn default-spit [loc stuff]
  (clojure.core/spit loc (pr-str stuff) :encoding "UTF-8"))

(defn pretty-spit [loc stuff]
  (with-open
    [ow (io/writer loc :encoding "UTF-8")]
    (pprint/pprint stuff ow)))
