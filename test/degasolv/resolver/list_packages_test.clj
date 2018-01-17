(ns degasolv.resolver.list-packages-test
  (:require [clojure.test :refer :all]
            [degasolv.resolver :refer :all]
            [clojure.core.match :refer [match]])
  (:import [degasolv.resolver
            PackageInfo
            Requirement
            DecoratedRequirement]))

(deftest ^:unit-tests list-packages-simple
  (testing "List an empty graph."
    (is (= []
           (list-packages {})))
    (is (= []
           (list-packages {:root []}))))
  (testing "Simple example"
    (let [simple-example
          {:root [:a :x :b]
           :a [:c :d]
           :c []
           :d []
           :x [:y :z]
           :b [:e :a]
           :e [:a]}]
      (is (= (list-packages simple-example :list-strat :lazy)
             [:c :d :y :z :a :e :x :b]))
      (is (= (list-packages simple-example :list-strat :eager)
             [:c :d :a :y :z :x :e :b])))))
