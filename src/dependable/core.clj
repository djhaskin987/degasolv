(ns dependable.core
  (:gen-class))

(defn resolve-dependencies
  [names
   query &
   {:keys [already-installed]
    :or {already-installed {}}}]
  (loop [remaining (seq names)
         installed already-installed
         result [:successful]]
    (if (empty? remaining)
      result
      (let [pkg (first remaining)
            r (rest remaining)
            pname (:name pkg)]
        (if (contains? installed pname)
          (recur r installed result)
          (let [response (query pname)]
            (if (empty? response)
              [:unsatisfiable pname]
              (let [chosen (first response)]
                (recur r (assoc
                           installed
                           (:name chosen)
                           (:version chosen))
                       (conj result chosen))))))))))

#_(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
