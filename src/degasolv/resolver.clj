(ns degasolv.resolver
  (:require [clojure.core.match :refer [match]]
            [degasolv.util :refer :all]))

#_(defmacro dbg [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
x#))

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

(def ^:private nil-safe-spec-call
  (fnil spec-call (fn [v] true)))

(defn- first-successful
  [result]
  (match
   result
   [:successful _] result
   :else nil))

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
                  (let [chk-ver (:version conj-val)
                        cmp-result (cmp pkg-ver chk-ver)]
                    (and conj-cum
                         (match (:relation conj-val)
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
                                :else
                                false)))) true disj-val)))
         false
         spec)))))

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
                                          (not (safe-spec-call  spec  present-package)))
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
                              (update-in absent-specs [id] conj spec)
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
                                           (reduce
                                            (fn [x y]
                                              (and
                                               x
                                               (not
                                                (safe-spec-call y candidate))))
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
