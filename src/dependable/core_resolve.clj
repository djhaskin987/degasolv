(defmacro
  prefer
  [thing other]
  `(let [x# ~thing]
     (if x# x# ~other)))

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
    [:successful _] result
    :else nil))

(defn- resolve-deps
  [repo
   present-packages
   found-packages
   absent-specs
   clauses]
  (if (empty? clauses)
    [:successful (set (vals found-packages))]
    (let [fclause (first clauses)
          rclauses (rest clauses)
          unsuccessful [:unsuccessful fclause]]
      (if (empty? fclause)
        unsuccessful
        (prefer
          (some
            first-successful
            (map
              (fn try-requirement
                [requirement]
                (let [{status :status id :id spec :spec} requirement
                      present-package (get present-packages id)]
                  (cond
                    (not (nil? present-package))
                    (when (or (and (= status :absent)
                                   (not (spec present-package))
                                   (and (= status :present)
                                        (spec present-package))))
                      (resolve-deps
                        repo
                        present-packages
                        found-packages
                        absent-specs
                        rclauses))
                    (= status :absent)
                    (resolve-deps
                      repo
                      present-packages
                      found-packages
                      (assoc-conj absent-specs id spec)
                      rclauses)
                    (= status :present)
                    (some
                      first-successful
                      (let [candidates (repo id)]
                        (map
                          (fn try-candidate
                            [candidate]
                            (resolve-deps
                              repo
                              (assoc present-packages id candidate)
                              (assoc found-packages id candidate)
                              absent-specs
                              (into rclauses (:requirements candidate))))
                          (filter
                            (fn vet-candidate
                              [candidate]
                              (let [absent-requirement (get absent-specs id)]
                                (if
                                  absent-requirement
                                  (reduce
                                    #(and %1 (not (%2 candidate)))
                                    true
                                    absent-requirement)
                                  true)))
                            candidates))))
                    :else nil)))
              fclause))
          unsuccessful)))))

(defn resolve-dependencies
  [specs
   query &
   {:keys [present-packages
           conflicts]
    :or {present-packages {}
         conflicts {}}}]
  (resolve-deps
    query
    present-packages
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
