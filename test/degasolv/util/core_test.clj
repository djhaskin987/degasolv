(ns degasolv.util.core_test
  (:require [clojure.test :refer :all]
            [degasolv.util :refer :all]))

(deftest ^:unit-tests assoc-conj-basic
  (testing "Add to a blank map"
    (is (.equals {:a [1]}
           (assoc-conj {} :a 1))))
  (testing "Add to a map with an empty list"
    (is (.equals {:a [1]}
           (assoc-conj {:a []} :a 1))))
  (testing "Add to a map with an existing list"
    (is (.equals {:a [1 2]}
           (assoc-conj {:a [1]} :a 2)))))
