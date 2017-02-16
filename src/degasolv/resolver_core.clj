(in-ns 'degasolv.resolver)
(defmacro dbg2 [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
x#))

(defrecord VersionPredicate [relation version])
(defrecord Requirement [status id spec])
(defrecord PackageInfo [id version location requirements])

; deprecated, do not use
(def ->requirement ->Requirement)
(def ->package ->PackageInfo)
(def ->version-predicate ->VersionPredicate)

(defn present
  ([id] (present id nil))
  ([id spec] (->Requirement :present id spec)))

(defn absent
  ([id] (absent id nil))
  ([id spec] (->Requirement :absent id spec)))

(defn- spec-call [f v]
  (f v))

(def ^:private nil-safe-spec-call
  (fnil spec-call (fn [v] true)))

(defn- first-successful
  [result]
  (if (and (sequential? result)
           (= (first result)
              :successful))
    result
    nil))

(defprotocol ^:private SpecCaller
  (p-safe-spec-call [this spec present-package]))

(extend-protocol SpecCaller
  nil
  (p-safe-spec-call [this spec present-package]
    (nil-safe-spec-call spec present-package))
  clojure.lang.IFn
  (p-safe-spec-call [cmp spec present-package]
    (if (nil? spec)
      true
      (let [pkg-ver (:version present-package)]
        (reduce
         (fn [disj-cum disj-val]
           (or disj-cum
               (reduce
                (fn [conj-cum conj-val]
                  conj-val
                  (let [chk-ver (:version conj-val)
                        cmp-result (cmp pkg-ver chk-ver)]
                    (and conj-cum
                         (case (:relation conj-val)
                                :greater-than
                                (pos? cmp-result)
                                :greater-equal
                                (not (neg? cmp-result))
                                :equal-to
                                (zero? cmp-result)
                                :not-equal
                                (not (zero? cmp-result))
                                :less-equal
                                (not (pos? cmp-result))
                                :less-than
                                (neg? cmp-result)
                                false)))) true disj-val)))
         false
         spec)))))

(defn- aggregate-attempts [c v]
  (conj c v))

(defn resolve-dependencies
  [requirements
   query & {:keys [present-packages
                   conflicts
                   strategy
                   compare]
            :or {present-packages {}
                 conflicts {}
                 strategy :thorough
                 compare nil}
            }]

  (let [safe-spec-call (partial p-safe-spec-call compare)
        cull (case strategy
               :thorough
               (fn [candidates] candidates)
               :fast
               (fn [candidates] [(first candidates)])
               (throw
                (ex-info (str
                          "Invalid strategy `"
                          strategy
                          "`.")
                         {:strategy strategy})))]
    (letfn [(resolve-deps
              [repo
               present-packages
               found-packages
               absent-specs
               clauses]
              (if (empty? clauses)
                [:successful (set (vals found-packages))]
                (let [fclause (first clauses)
                      rclauses (rest clauses)]
                  (if (empty? fclause)
                    [:unsuccessful
                     {:problems
                      [{:term fclause
                        :found-packages found-packages
                        :present-packages present-packages
                        :absent-specs absent-specs
                        :reason :empty-alternative-set}]}]
                    (let [clause-result
                          (map
                           (fn try-requirement
                             [requirement]
                             (let [{status :status id :id spec :spec}
                                   requirement
                                   present-package
                                   (or (get present-packages id)
                                       (get found-packages id))]
                               (cond
                                 (not (nil? present-package))
                                 (if (or (and (= status :absent)
                                              (not (safe-spec-call  spec  present-package)))
                                         (and (= status :present)
                                              (safe-spec-call spec present-package)))
                                   (resolve-deps
                                    repo
                                    present-packages
                                    found-packages
                                    absent-specs
                                    rclauses)
                                   [:unsuccessful
                                    {:problems
                                     [
                                      {:term fclause
                                       :requirement requirement
                                       :found-packages found-packages
                                       :present-packages present-packages
                                       :absent-specs absent-specs
                                       :reason :present-package-conflict
                                       :package id}]}])
                                 (= status :absent)
                                 (resolve-deps
                                  repo
                                  present-packages
                                  found-packages
                                  (update-in
                                   absent-specs
                                   [id] conj spec)
                                  rclauses)
                                 (= status :present)
                                 (let [query-results (repo id)]
                                   (if (empty? query-results)
                                     [:unsuccessful
                                      {:problems
                                       [
                                        {:term fclause
                                         :requirement requirement
                                         :found-packages found-packages
                                         :present-packages present-packages
                                         :absent-specs absent-specs
                                         :reason :package-not-found
                                         :package id}]}]
                                     (let [filtered-query-results
                                           (cull
                                            (filter
                                             (fn vet-candidate
                                               [candidate]
                                               (and
                                                (safe-spec-call spec candidate)
                                                (reduce
                                                 (fn [x y]
                                                   (and
                                                    x
                                                    (not
                                                     (safe-spec-call
                                                      y
                                                      candidate))))
                                                 true
                                                 (get absent-specs id))))
                                             query-results))]
                                       (if (empty? filtered-query-results)
                                         [:unsuccessful
                                          {:problems
                                           [{
                                             :requirement requirement
                                             :found-packages found-packages
                                             :present-packages present-packages
                                             :absent-specs absent-specs
                                             :reason :package-rejected
                                             :package id}]}]
                                         (let [candidate-results
                                               (map
                                                #(resolve-deps
                                                  repo
                                                  present-packages
                                                  (assoc found-packages id %)
                                                  absent-specs
                                                  (into rclauses
                                                        (:requirements %)))
                                                filtered-query-results)]
                                           (or
                                            (some
                                             first-successful
                                             candidate-results)
                                            [:unsuccessful
                                             {:problems
                                              (flatten
                                               (map
                                                #(:problems
                                                  (get % 1))
                                                candidate-results))}]))))))
                                 :else
                                 [:unsuccessful {:problems
                                                 [{:term fclause
                                                   :reason :uncovered-case
                                                   :requirement requirement
                                                   :found-packages found-packages
                                                   :present-packages present-packages
                                                   :absent-specs absent-specs}]}])))
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
                      (or
                       (some
                        first-successful
                        clause-result)
                       [:unsuccessful
                        {:problems
                         (flatten
                          (map
                           #(:problems
                             (get % 1))
                           clause-result))}]))))))]
      (resolve-deps
       query
       present-packages
       {}
       conflicts
       requirements))))
