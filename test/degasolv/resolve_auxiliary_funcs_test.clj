(ns degasolv.resolve-auxiliary-funcs-test
  (:require [clojure.test :refer :all]
            [degasolv.resolver :refer :all]
            [clojure.core.match :refer [match]]
            [serovers.core :refer [maven-vercmp]
             :rename {maven-vercmp cmp}])
  (:import [degasolv.resolver
            PackageInfo
            Requirement]))

(deftest cull-nothing-test
  (testing "cull nothing function - empty case"
    (is (= (#'degasolv.resolver/cull-nothing [])
           [])))
  (testing "cull nothing funciton - single case"
    (is (= (#'degasolv.resolver/cull-nothing [1])
           [1])))
  (testing "cull nothing function - many case"
    (is (= (#'degasolv.resolver/cull-nothing [1 2 3])
        [1 2 3]))))

(deftest cull-all-but-first-test
  (testing "cull all but first - empty case"
    (is (= [] (#'degasolv.resolver/cull-all-but-first []))))
  (testing "cull all but first - single case"
    (is (= [1] (#'degasolv.resolver/cull-all-but-first [1]))))
  (testing "cull all but first - many case"
    (is (= [1] (#'degasolv.resolver/cull-all-but-first [1 2 3])))))
