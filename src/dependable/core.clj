(ns dependable.core
  (:gen-class))


(defn spec-call [f v]
  (f v))

(def safe-spec-call
  (fnil spec-call (fn [v] true)))

(defn resolve-dependencies
  [names
   query &
   {:keys [already-installed
           conflicts]
    :or {already-installed {}
         conflicts {}}}]
  (loop [remaining (seq names)
         installed already-installed
         conflict conflicts
         result [:successful]]
    (if (empty? remaining)
      result
      (let [pkg (first remaining)
            r (rest remaining)
            pname (:name pkg)
            pspec (:version-spec pkg)]
        (cond (contains? installed pname)
              (if (not (safe-spec-call pspec (installed pname)))
                [:unsatisfiable pname]
                (recur r installed conflict result))
              (and (contains? conflict pname)
                   (nil? (conflict pname)))
              [:unsatisfiable pname]
              :else
              (let [response (query pname)
                    chosen (first
                             (filter
                               #(safe-spec-call pspec (:version %))
                               response))
                    chosen-conflicts (:conflicts chosen)]
                (if (or (nil? chosen)
                        (and (contains? conflict pname)
                               (safe-spec-call (conflict pname) (:version chosen))))
                      [:unsatisfiable pname]
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
