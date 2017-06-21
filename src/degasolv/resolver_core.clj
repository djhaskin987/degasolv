(in-ns 'degasolv.resolver)

(defmacro dbg2 [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
x#))

(def ^:private relation-strings
  {:greater-than ">"
   :greater-equal ">="
   :equal-to "=="
   :not-equal "!="
   :less-equal "<="
   :less-than "<"})

(defrecord VersionPredicate [relation version]
  Object
  (toString [this]
    (str
     ((:relation this) relation-strings)
     version)))

(defrecord Requirement [status id spec]
  Object
  (toString [this]
    (str
     (if (= (:status this) :absent)
       "!"
       "")
     (:id this)
     (clj-str/join
      ";"
      (map
       (fn conjoin-preds [conjunction]
         (clj-str/join
          ","
          (map
           #(str %)
           conjunction)))
       (:spec this))))))

(defrecord PackageInfo [id version location requirements])

; deprecated, do not use
(def ->requirement ->Requirement)
(def ->package ->PackageInfo)
(def ->version-predicate ->VersionPredicate)

(defmethod
  print-method
  degasolv.resolver.PackageInfo
  [this w]
  (tag/pr-tagged-record-on this w))

(defmethod
  print-method
  degasolv.resolver.VersionPredicate
  [this w]
  (tag/pr-tagged-record-on this w))

(defmethod
  print-method
  degasolv.resolver.Requirement
  [this w]
  (tag/pr-tagged-record-on this w))

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

(defn make-spec-call [cmp]
  (partial p-safe-spec-call cmp))

(defn- cull-nothing [candidates]
  candidates)

(defn- cull-all-but-first [candidates]
  [(first candidates)])


(defn- hoist [alternatives
              absent-specs
              found-packages
              present-packages]
  (if (= 1 (count alternatives))
    alternatives
    (let [partn
          (group-by
            (fn [term]
              (let [id (get term :id)]
                (cond
                  (get
                    absent-specs
                    id)
                  :absent
                  (or
                    (get
                      found-packages
                      id)
                    (get
                      present-packages
                      id))
                  :present
                  :else
                  :unspecified)))
            alternatives)]
      (concat (:absent partn) (:present partn) (:unspecified partn)))))


(defn resolve-dependencies
  [requirements
   query & {:keys [present-packages
                   conflicts
                   strategy
                   conflict-strat
                   compare
                   allow-alternatives]
            :or {present-packages {}
                 conflicts {}
                 strategy :thorough
                 conflict-strat :exclusive
                 compare nil
                 allow-alternatives true}}]
  (let [safe-spec-call (make-spec-call compare)
        cull (case strategy
                 :thorough
                 cull-nothing
                 :fast
                 cull-all-but-first
                 (throw
                  (ex-info (str
                            "Invalid strategy `"
                            strategy
                            "`.")
                           {:strategy strategy})))
        cull-alternatives
        (if allow-alternatives
          cull-nothing
          cull-all-but-first)]
    (letfn [(resolve-deps
              [repo
               present-packages
               found-packages
               absent-specs
               clauses]
              (if (empty? clauses)
                (if (= conflict-strat :inclusive)
                  [:successful (set (flatten (vals found-packages)))]
                  [:successful (set (vals found-packages))])
                (let [fclause (first clauses)
                      rclauses (subvec clauses 1)]
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
                           (fn try-alternative
                             [alternative]
                             (let [{status :status id :id spec :spec}
                                   alternative
                                   present-package
                                   (or (get present-packages id)
                                       (get found-packages id))]
                               (cond
                                 (and (not (= conflict-strat :inclusive))
                                           (not (nil? present-package)))
                                 (if (or (= conflict-strat :prioritized)
                                         (and (= status :absent)
                                              (not (safe-spec-call spec present-package)))
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
                                       :found-packages found-packages
                                       :present-packages present-packages
                                       :absent-specs absent-specs
                                       :reason :present-package-conflict
                                       :alternative alternative
                                       :package-id id}]}])
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
                                         :alternative alternative
                                         :found-packages found-packages
                                         :present-packages present-packages
                                         :absent-specs absent-specs
                                         :reason :package-not-found
                                         :package-id id}]}]
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
                                           [{:term fclause
                                             :alternative alternative
                                             :found-packages found-packages
                                             :present-packages present-packages
                                             :absent-specs absent-specs
                                             :reason :package-rejected
                                             :package-id id}]}]
                                         (let [candidate-results
                                               (map
                                                #(resolve-deps
                                                  repo
                                                  present-packages
                                                  (if (= conflict-strat :inclusive)
                                                    (update-in found-packages [id]
                                                               conj
                                                               %)
                                                    (assoc found-packages
                                                           id %))
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
                                                   :alternative alternative
                                                   :found-packages found-packages
                                                   :present-packages present-packages
                                                   :absent-specs absent-specs}]}])))
                           ;; Hoisting
                           (hoist (cull-alternatives
                                    fclause)
                                  absent-specs
                                  found-packages
                                  present-packages))]
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
       (vec requirements)))))
