(ns degasolv.resolver.list-packages-test
  (:require [clojure.test :refer :all]
            [degasolv.resolver :refer :all]
            [clojure.core.match :refer [match]])
  (:import [degasolv.resolver
            PackageInfo
            Requirement
            DecoratedRequirement]))

(deftest ^:unit-tests list-packages-function
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
             [:c :d :a :y :z :x :e :b]))))
  (testing "In the case of a circular dep, the shallower dep is listed last, and peer deps are listed in order."
    (let [circular-example
          {:root [:a]
           :a [:b]
           :b [:a]}]
      (is (= (list-packages circular-example :list-strat :lazy)
             [:b :a]))
      (is (= (list-packages circular-example :list-strat :eager)
             [:b :a])))
    (let [circular-triangle
          {:root [:a :b]
           :a [:c]
           :b [:c :d]
           :d [:a :x]
           :x []
           :c [:a]}]
      (is (= (list-packages circular-triangle :list-strat :eager)
             [:c :a :x :d :b])
          (= (list-packages circular-triangle :list-strat :lazy)
             [:c :x :d :a :b]))))
  (testing "In the case that I rely on myself"
    (let [awful-example
          {:root [:a]
           :a [:a]}]
      (is (= (list-packages awful-example :list-strat :eager)
             [:a]))
      (is (= (list-packages awful-example :list-strat :lazy)
             [:a])))))
