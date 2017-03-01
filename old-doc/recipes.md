Merging two repository datastructures
=====================================

```clojure
(merge-with
  (fn [x y]
    (sort
      #(cmp (:version %1) (:version %2))
      (concat x y)))
  (clojure.edn/read-str (slurp "./index-dee.edn"))
  (clojure.edn/read-str (slurp "./index-dum.edn")))
```

Adding a record to a repository index
=====================================

```clojure
(let [new-record (clojure.edn/read-str (slurp "./dependable.edn"))]
  (update-in
    (clojure.edn/read-str (slurp "url-to-index.edn"))
    [(:id new-record)]
    conj
    new-record))
```

Managed dependencies
====================

Under Construction.
