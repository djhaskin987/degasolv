(ns dependable.core
  (:gen-class))

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
            pname (:name pkg)]
        (cond (contains? installed pname)
              (recur r installed conflict result)
              (contains? conflict pname)
              [:unsatisfiable pname]
              :else
              (let [response (query pname)]
                (if (empty? response)
                  [:unsatisfiable pname]
                  (let [chosen (first response)
                        chosen-conflicts (:conflicts chosen)]
                    (recur r (assoc
                               installed
                               (:name chosen)
                               (:version chosen))
                           (into
                             conflict
                             chosen-conflicts)
                           (conj result chosen))))))))))

#_(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
