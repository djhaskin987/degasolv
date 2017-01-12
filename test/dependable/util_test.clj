(ns dependable.util-test
  (:require [clojure.test :refer :all]
            [dependable.util :refer :all]))

(deftest ^:util assoc-conj-basic
  (testing "Add to a blank map"
    (is (= {:a [1]}
           (assoc-conj {} :a 1))))
  (testing "Add to a map with an empty list"
    (is (= {:a [1]}
           (assoc-conj {:a []} :a 1))))
  (testing "Add to a map with an existing list"
    (is (= {:a [1 2]}
           (assoc-conj {:a [1]} :a 2)))))
