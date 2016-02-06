(ns dependable.core
  (:gen-class))


(defn resolve-dependencies
  [names
   query &
   [:keys [already-installed]
    :or {already-installed {}}]]
  (loop [remaining (seq names)
         installed already-installed
         result [:successful]]
    (if (empty? remaining)
      result
      (let [pkg (first remaining)
            r (rest remaining)
            pname (:name pkg)
            response (query pname)]
        (cond (empty? response) [:unsatisfiable pname]
              (contains? installed pname) (recur r installed result)
              :else
              (let [chosen (first response)]
                (recur r (assoc
                           installed
                           (:name chosen)
                           (:version chosen))
                       (conj result chosen))))))))

#_(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
