(ns dependable.core-test
  (:require [clojure.test :refer :all]
            [dependable.core :refer :all]))

(defn map-query [m]
  (fn [nm]
    (let [result (find m nm)]
      (if (nil? result)
        []
        (let [[k v] result]
          v)))))

(let  [package-a30
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
       query (map-query repo-info)]
  (deftest simple-find
           (testing "Asking for a present package succeeds."
                    (is (= (resolve-dependencies
                             [{:name "a"}]
                             query)
                           [:successful package-a30])))
                    (is (= (resolve-dependencies
                             [{:name "c"}]
                             query)
                           [:successful package-c10]))
           (testing "Asking for a nonexistent package fails."
                    (is (= (resolve-dependencies
                             [{:name "b"}]
                             query)
                           [:unsatisfiable "b"]))))
  (deftest already-installed
           (testing "Asking to install a package twice."
                    (is (= (resolve-dependencies
                             [{:name "a"}
                              {:name "a"}]
                             query)
                           [:successful package-a30])))
           (testing (str "Asking to install a package that I have given as already "
                    "installed.")
                    (is (= (resolve-dependencies
                             [{:name "c"}]
                             :already-installed
                             {"c" 18})
                           [:successful])))
           (testing (str "Asking to install a package that I have given as already "
                         "installed, even though the package isn't availalbe.")
                    (is (= (resolve-dependencies
                             [{:name "b"}]
                             :already-installed
                             {"b" 30})
                           [:successful])))))
