(ns dependable.core
  (:gen-class))

; Helper functions

(defn spec-call [f v]
  (f v))

(def safe-spec-call
  (fnil spec-call (fn [v] true)))


#_(defmacro debug [form]
  `(let [x# ~form]
     (println (str "Debug: "
                   (quote ~form)
                   " is " x#))
     x#))

; Tested functions
(defn remove-dep
  [to from node]
  (let [children (:children (to node))
        children-names (keys children)
        from' (reduce
                (fn [c [child _parents]]
                  (if (= (count _parents) 1)
                    (dissoc c child)
                    (assoc c
                           child
                           (disj _parents node))))
                from
                (map #(find from %)
                     children-names))
        to' (dissoc to
                    node)]
    (reduce (fn [c [nm version-spec]]
              (let [[to'' from''] c]
                (if (contains? from' nm)
                  c
                  (remove-dep to'' from'' nm))))
            [to' from']
            children)))






(defn realize
  "Takes a request object and a query object and
  returns an actual package object pull from the
  query object, which satisfies the request"
  [query request]
  (first
    (filter
      #(safe-spec-call (:version-spec request) (:version %))
      (query (:name request)))))





;(->package-graph)
;{:records {"name" <package>}
; :dependee-parents {"dependee" #{"depender" "depender}}}

(defrecord package-graph [records dependee-parents])
(defrecord package [name version location dependencies])
(defn resolve-dependencies [package]
  (reduce
    reconcile
    (->package-graph {(:name package) package} {})
    (map #(resolve-dependencies
            (realize %1))
         (:dependencies package))))


; realize is already written
; needs work to be able to handle conflicts, already-installeds, etc.
; Therefore: ONLY SOLVE THE REQUIRES / DIAMOND TESTS FIRST
(defn reconcile [a b]
  (let [a-package-lines (set (keys (:records a)))
        b-package-lines (set (keys (:records b)))
        competing-lines
        (intersection
          a-package-lines
          b-package-lines)]
    ; take two graphs a and b
    ; graphs are lookup from "line name" (the package name, not from the
    ; package, but the package spec it fulfills)
    ;
    ; find the intersection of the package lines, called "competing lines"
    ;
    ; construct possible-incompletes, the set of all lines which depends on any
    ; of the competing lines
    ;
    ; construct a' from a, where all the competing lines have been removed a la
    ; remove-lines and keeping track of any lines which are not yet realized
    ;
    ; construct b' from b, where all the competing lines have been removed a la
    ; remove-lines and keeping track of any lines which are not yet realized
    ;
    ; construct result from (->package-graph (into (:records a') (:records b'))
    ;                                        (into (:parents a') (:parents b'))
    ;                                        (into (:left-hanging a') (:left-hanging b')))
    ; you can do this b/c a' and b' are mututally exclusive
    ;
    ; now we just need to know what needs to be re-resolved. So, for each node
    ; in the graph from listed in possible-incompletes, we check for any dependencies
    ; of those children which are not yet resolved. if any aren't, we resolve them
    ; using all available specs from the current graph and add the packages to the
    ; graph.
    ; (reconcile graph-so-far (resolve-dependencies (:name same-name :spec combined-specs)))
    ;
    ; I just need to define some good test cases, work them manually, and then
    ; codify my process.
    ;
    ; TODO define set of cases for just requires & diamonds
    ; TODO THEN ship it. lean methodology
    ; TODO remove-lines needs to handle cycles, ex. a -> b, b -> c, b -> d, d -> b, remove b.
    (reduce (fn [graph [package-line package]]
              (if ((:records graph) package-line)
                (let [all-specs #(and ((specs-for package-line a) %1)
                                     ((specs-for package-line b) %1))]

                  (remove-line graph package-line)

              (

    (into (into
            {}
            (map (fn [x] (if (competing-packages x)
                           (reconcile resolve
    ; compute the specs for each competing package
    ; realize and resolve each competing package
    ; then, when adding stuff from a and then b into the result graph,
    ; if the package is a "competing" package, add it from competing packages,
    ; else add it from a/b.
    ))

(defn resolve-dependencies [graph package]



(defn resolve-dependencies
  [specs
   query &
   {:keys [already-found
           conflicts]
    :or {already-found {}
         conflicts {}}}]
  (loop [remaining (seq specs)
         installed already-found
         conflict conflicts
         result #{}]
    (if (empty? remaining)
      [:successful result]
      (let [pkg (first remaining)
            r (rest remaining)
            pname (:name pkg)
            pspec (:version-spec pkg)]
        (cond (contains? installed pname)
              (if (not (safe-spec-call pspec (installed pname)))
                [:unsatisfiable [pname]]
                (recur r installed conflict result))
              (and (contains? conflict pname)
                   (nil? (conflict pname)))
              [:unsatisfiable [pname]]
              :else
              (let [chosen (choose-candidate pname pspec query)
                    chosen-conflicts (:conflicts chosen)]
                (if (or (nil? chosen)
                        (and (contains? conflict pname)
                             (safe-spec-call
                               (conflict pname)
                               (:version chosen))))
                  [:unsatisfiable [pname]]
                  (recur r (assoc
                             installed
                             (:name chosen)
                             (:version chosen))
                         (into
                           conflict
                           chosen-conflicts)
                         (conj result chosen)))))))))

#_(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
