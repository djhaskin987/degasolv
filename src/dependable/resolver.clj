>(ns dependable.resolver
  (:require [clojure.core.match :refer [match]]
             [dependable.util :refer :all]))

(defrecord requirement [status id spec])

(defn map-query [m]
  (fn [nm]
    (let [result (find m nm)]
      (if (nil? result)
        []
        (let [[k v] result]
          v)))))

(defn present
  ([id] (present id nil))
  ([id spec] (->requirement :present id spec)))

(defn absent
  ([id] (absent id nil))
  ([id spec] (->requirement :absent id spec)))

(defrecord package [id version location requirements])

(defn- spec-call [f v]
  (f v))

(def nil-safe-spec-call
  (fnil spec-call (fn [v] true)))

(defn- first-successful
  [result]
  (match
   result
   [:successful _] result
   :else nil))

(defprotocol SpecCaller
  (p-safe-spec-call [this spec present-package]))
(extend-protocol SpecCaller
  nil
  (p-safe-spec-call [this spec present-package]
    (nil-safe-spec-call spec present-package))
  clojure.lang.IFn
  (p-safe-spec-call [less spec present-package]
    (if (nil? spec)
      true
      (let [pkg-ver (:version present-package)]
        (reduce
         (fn [cum val]
           (let [chk-ver (:version val)]
             (and cum
                  (match val
                         {:relation :greater-than :version v}
                         (less chk-ver pkg-ver)
                         {:relation :greater-equal :version v}
                         (not (less pkg-ver chk-ver))
                         {:relation :less-equal :version v}
                         (not (less chk-ver pkg-ver))
                         {:relation :equal-to :version v}
                         (and (not (less pkg-ver chk-ver))
                              (not (less chk-ver pkg-ver)))
                         :else
                         false)))) true spec))))
  )


(defn resolve-dependencies
  [specs
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
        cull (match strategy
                    :thorough
                    (fn [candidates] candidates)
                    :fast
                    (fn [candidates] (first candidates))
                    :else
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
                      rclauses (rest clauses)
                      unsuccessful [:unsuccessful fclause]]
                  (if (empty? fclause)
                    unsuccessful
                    (or
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
                                 (cull (filter
                                        (fn vet-candidate
                                          [candidate]
                                          (and
                                           (safe-spec-call spec candidate)
                                           (reduce (fn [x y]
                                                     (and x (not (safe-spec-call y candidate))))
                                                   true
                                                   (get absent-specs id))))
                                        candidates)))))
                             :else nil)))
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
                           (concat (:absent partn) (:present partn) (:unspecified partn))))))
                     unsuccessful)))))]
         (resolve-deps
          query
          present-packages
          {}
          conflicts
          specs))))
