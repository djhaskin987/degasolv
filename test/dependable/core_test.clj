(ns dependable.core-test
  (:require [clojure.test :refer :all]
            [dependable.core :refer :all]))

(deftest simple-find
         (let [package-a30
               {:name "a"
                :version 30
                :url "a_loc30"}
               package-a20
               {:name "a"
                :version 20
                :url "a_loc20"}
               package-c10
               {:name "c"
                :version 10
                :url "c_loc10"}
               repo-info
               {"a"
                [package-a30
                 package-a20]
                "c"
                [package-c10]}
               query
               (fn [nm]
                 (let [result (find repo-info nm)]
                   (if (nil? result)
                     []
                     (get result 1))))]
           (testing "Asking for a present package succeeds."
                    (is (= (resolve-dependencies
                             [{:name "a"}]
                             query)
                           [:successful [package-a30]])))
           (testing "Asking for a nonexistent package fails."
                    (is (= (resolve-dependencies
                             [{:name "b"
                               :trace :b-isnt-there}]
                             query)
                           [:unsatisfiable [:b-isnt-there]])))))
