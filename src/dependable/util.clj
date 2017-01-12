(ns dependable.util)

(defn assoc-conj
  [mp k v]
  (if (empty? mp)
    {k [v]}
    (if (empty? (get mp k))
      (assoc mp k [v])
      (assoc mp k
             (conj
              (mp k)
              v)))))
