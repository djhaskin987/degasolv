(ns degasolv.util)

; deprecated, we should just use update-in
(defn assoc-conj
  [mp k v]
  (update-in mp [k] conj v))

; UTF-8 by default :)
(defn- default-slurp [loc]
  (clojure.core/slurp loc :encoding "UTF-8"))

(defn- default-spit [loc stuff]
  (clojure.core/spit loc (pr-str stuff) :encoding "UTF-8"))
