(ns dependable.core-test
  (:require [clojure.test :refer :all]
            [dependable.core :refer :all]))

(deftest simple-find
         (let [package-a
               {:name "a"
                :version 30
                :url "a_loc30"}
               repo-info
               {"a"
                [package-a]}
               query (fn [nm]
                       (let [result (find repo-info nm)]
                         (if (nil? result)
                           :unsatisfiable
                           (get result 1))))]
           (testing (is (= (resolve-dependencies
                                 ["a"]
                                 query)
                           [package-a])))
           (testing (is (= (resolve-dependencies
                             ["b"]
                             query)
                           [:unsatisfiable])))))
