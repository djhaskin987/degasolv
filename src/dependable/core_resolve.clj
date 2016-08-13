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
                   (when
                       (or (and (= status :absent)
                                (not (safe-spec-call spec present-package)))
                           (and (= status :present)
                                (safe-spec-call spec present-package)))
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
                        (and
                         (safe-spec-call spec candidate)
                         (reduce (fn [x y]
                          (and x (not (safe-spec-call y candidate))))
                          true
                          (get absent-specs id))))
                      candidates))))
                 :else nil)))
           fclause))
         unsuccessful)))))

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

#_(defn -main
    "I don't do a whole lot ... yet."
    [& args]
    (println "Hello, World!"))
