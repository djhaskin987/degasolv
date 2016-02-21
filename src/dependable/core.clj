(ns dependable.core
  (:gen-class))

; Helper functions

(defn spec-call [f v]
  (f v))

(def safe-spec-call
  (fnil spec-call (fn [v] true)))

; Tested functions

(defn realize
  "Takes a request object and a query object and
  returns an actual package object pull from the
  query object, which satisfies the request"
  [query request]
  (first
    (filter
      #(safe-spec-call (:version-spec request) (:version %))
      (query (:name request)))))

(defn remove-dep
  [to from node]
  (let [from' (reduce
              (fn [c [child _parents]]
                (if (= (count _parents) 1)
                  c
                  (assoc c
                         child
                         (dissoc _parents node))))
              from
              (map #(find from %)
                   (keys (:children node))))
        to' (dissoc to
                    node)]
      (reduce (fn [c v]
                (let [[to'' from''] c]
                  (remove-dep to'' from'' v)))
          [to' from']
          (:children node))))

(defn patch-graph
  [graph patch]
  (reduce-kv
    (fn [cmt k v]
      (assoc cmt k
             (assoc v
                    :children
                    (map
                      (fn [x] (patch-graph x patch))
                      (v :children)))))
    {}
    graph))




(defn choose-candidate
  [pname
   pspec
   query]
  (let [response (query pname)]
    (first
      (filter
        #(safe-spec-call pspec (:version %))
        response))))

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
