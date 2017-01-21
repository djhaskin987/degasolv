(ns degasolv.util)


; deprecated, we should just use update-in
(defn assoc-conj
  [mp k v]
  (update-in mp [k] conj v))
