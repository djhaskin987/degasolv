(ns dependable.resolve)
(require '[clojure.core.match :refer [match]])

(defrecord literal [polarity id spec])
(defrecord package [id version location requirements])

(defmacro
  prefer
  [thing other]
  `(let [x# ~thing]
     (if x# x# ~other)))

; Helper functions and macros
(defmacro debug [form]
  `(let [x# ~form]
     (println
       (str
         "Debug: "
         (quote ~form)
         " is "
         x#))
     x#))

(defn assoc-conj
  [mp k v]
  (if (empty? mp)
    {k [v]}
    (if (empty? (get mp k))
      (assoc mp [v])
      (assoc mp
             (conj
               (mp k)
               v)))))

(defn spec-call [f v]
  (f v))

(def safe-spec-call
  (fnil spec-call (fn [v] true)))

(defn- first-successful
  [result]
  (match
    result
    [:satisfied _] result
    :else nil))

(defn- resolve-deps
  [repo
   present-packages
   found-packages
   absent-specs
   clauses]
  (if (empty? clauses)
    [:successful found-packages]
    (let [fclause (first clause)
          rclauses (rest clause)
          unsuccessful [:unsatisfiable fclause]]
      (if (empty? fclause)
        unsuccessful
        (prefer
          (some
            first-successful
            (map
              (fn try-literal
                [literal]
                (let [{polarity :polarity id :id spec :spec} literal
                      present-package (get present-packages id)]
                  (cond
                    (not (nil? present-package))
                    (when (or (and (= polarity :absent)
                                   (not (spec present-package))
                                   (and (= polarity :present)
                                        (spec present-package))))
                      (resolve-deps
                        repo
                        present-packages
                        found-packages
                        absent-specs
                        rclauses))
                    (= polarity :absent)
                    (resolve-deps
                      repo
                      present-packages
                      found-packages
                      (assoc-conj absent-specs id spec)
                      rclauses)
                    (= polarity :present)
                    (some
                      first-successful
                      (let [candidates (repo id)]
                        (map
                          (fn try-candidate
                            [candidate]
                            (resolve-deps
                              repo
                              (assoc present-packages candidate)
                              (assoc found-packages candidate)
                              absent-specs
                              (into rclauses (:requirements candidate))))
                          (filter
                            (fn vet-candidate
                              [candidate]
                              (let [absent-literal (get absent-specs id)]
                                (if
                                  absent-literal
                                  (reduce
                                    #(and %1 (not (%2 candidate)))
                                    true
                                    absent-literal)
                                  true)))
                            candidates))))
                    :else nil)))
              fclause))
          unsuccessful)))))

(defn resolve-dependencies
  [specs
   query &
   {:keys [already-found
           conflicts]
    :or {already-found {}
         conflicts {}}}]
  (resolve-deps
    query
    already-found
    {}
    conflicts
    specs))

#_((loop [remaining (seq specs)
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
