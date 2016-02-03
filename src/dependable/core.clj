(ns dependable.core
  (:gen-class))


(defn resolve-dependencies
  [names
   query]
  (reduce (fn [c v]
            (let [result
                  (query v)]
                  (if (= result :unsatisfiable)
                    (conj c result)
                    (conj c (first result)))))
          []
          names))

#_(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
