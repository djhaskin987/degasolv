(defmacro debug [form]
  `(let [x# ~form]
     (println (str "Debug: `" (quote ~form)
                   "` is `" (pr-str x#)
                   "`"))
     x#))

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
      (assoc mp k [v])
      (assoc mp k
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
        (let [results
              (map
               (fn try-requirement
                 [requirement]
                 (let [{status :status id :id spec :spec} requirement
                       present-package (get present-packages id)
                       absent-term-specs (get absent-specs id)]
                   (cond
                     (not (nil? present-package))
                     (if
                         (or (and (= status :absent)
                                  (not (safe-spec-call spec present-package)))
                             (and (= status :present)
                                  (safe-spec-call spec present-package)))
                       (resolve-deps
                        repo
                        present-packages
                        found-packages
                        absent-specs
                        rclauses)
                       [:incompatible id spec])
                     (= status :absent)
                     (resolve-deps
                      repo
                      present-packages
                      found-packages
                      (assoc-conj absent-specs id spec)
                      rclauses)
                     (= status :present)
                     (let [candidates
                           (repo id)
                           allowed-candidates
                           (filter
                            (fn check-candidate
                              [candidate]
                              (not (reduce
                                    #(or %1 %2)
                                    false
                                    (map
                                     #(% candidate)
                                     absent-term-specs))))
                            candidates)]
                       (if (empty? allowed-candidates)
                         [:forbidden id]
                         (some
                          first-successful
                          (map
                           (fn try-candidate
                             [candidate]
                             (resolve-deps
                              repo
                              (assoc present-packages id candidate)
                              (assoc found-packages id candidate)
                              absent-specs
                              (into rclauses (:requirements candidate))))
                           allowed-candidates)))))))
               ;; Hoisting
               (if (= 1 (count fclause))
                 fclause
                 (let [partn
                       (group-by
                        (fn [term]
                          (let [id (get term :id)]
                            (cond
                              (get
                               absent-specs
                               id)
                              :absent
                              (get
                               present-packages
                               id)
                              :present
                              :else
                              :unspecified)))
                        fclause)]
                   (concat (:absent partn) (:present partn) (:unspecified partn)))))]
          (prefer
           (some
            first-successful
            results)
           (let [result-groups
                 (group-by
                  (fn [result]
                    (if (or (= :incompatible (first result))
                            (= :forbidden (first result)))
                      :incompatible
                      :unsuccessful))
                  results)]
             (first
              (conj
               (:incompatible result-groups)
               unsuccessful)))))))))

(defn resolve-dependencies
  [specs
   query & thing]
  (let [{:keys [present-packages
           conflicts]
          :or {present-packages {}
               conflicts {}}} thing]
  (resolve-deps
   query
   present-packages
   {}
   conflicts
   specs)))

;; some notes on how to program in the category dependency assumption
;; where 'a spec' is the function which examines candidates
;; this works well because candidates are first examined for hoisted terms
#_(letfn [(a [spec] ...)]
    (loop [spec spec]
      (let [rs (a spec)]
        (match rs
               [:incompatible my_id s]
               (recur #(and (s %) (spec %)))
               :else
               rs))))
#_(defn -main
    "I don't do a whole lot ... yet."
    [& args]
    (println "Hello, World!"))
