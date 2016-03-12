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





;[package->dependencies dependee->dependers]
;{:packageToDependencies {"name" {:package {}
;                :children {"name" spec
;                           "name" spec}}}
; :dependeeToDependers {"dependee" ["depender" "depender"]}}

(defrecord package-graph [records dependee-parents])

; realize is already written
; needs work to be able to handle conflicts, already-installeds, etc.
; Therefore: ONLY SOLVE THE REQUIRES / DIAMOND TESTS FIRST
(defn reconcile [a b]
  (let [a-package-lines (set (keys (:records a)))
        b-package-lines (set (keys (:records b)))
        competing-packages
        (intersection
          a-package-lines
          b-package-lines)]
    ; compute the specs for each competing package
    ; realize and resolve each competing package
    ; then, when adding stuff from a and then b into the result graph,
    ; if the package is a "competing" package, add it from competing packages,
    ; else add it from a/b.
    ))


(defn resolve-dependencies [dep]
  (reduce
    reconcile
    [{} {}]
    (map #(resolve-dependencies (realize %1)) (:children dep))))



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
