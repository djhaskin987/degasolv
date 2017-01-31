(ns degasolv.resolve-repo-aggregation-test
  (:require [clojure.test :refer :all]
            [degasolv.resolver :refer :all]
            [clj-semver.core :refer [cmp]]))

(deftest ^:repo-aggregation priority-repo-test
  "Testing that priority-repo works"
  (testing "Empty case"
    (is (= []
         ((priority-repo [(map-query {})
                          (map-query {})]) "a"))))
  (testing "Query a repo for something not found"
    (is (= []
           ((priority-repo [(map-query {"b" [{:id "b" :version 1 :location "loc_b1"}]})
                            (map-query {"c" [{:id "c" :version 2 :location "loc_c2"}]})])
            "a"))))
  (testing "Empty repo is skipped over"
    (is (= [{:id "a" :version 10 :location "loc_a"}]
           ((priority-repo [(map-query {})
                            (map-query {"a" [{:id "a" :version 10 :location "loc_a"}]})])
            "a"))))
  (testing "The first repo that has packages wins"
    (is (= [{:id "a" :version 10 :location "loc_a10"}]
           ((priority-repo [(map-query {})
                            (map-query {"a" [{:id "a" :version 10 :location "loc_a10"}]})
                            (map-query {"a" [{:id "a" :version 20 :location "loc_a20"}
                                             {:id "a" :version 30 :location "loc_a30"}]})])
            "a")))))

(deftest ^:repo-aggregation global-repo-test
  "Testing that priority-repo works"
  (testing "Empty case"
    (is (= []
           ((global-repo [(map-query {})
                           (map-query {})]) "a"))))
  (testing "Query a repo for something not found"
    (is (= []
           ((global-repo [(map-query {"b" [{:id "b" :version 1 :location "loc_b1"}]})
                            (map-query {"c" [{:id "c" :version 2 :location "loc_c2"}]})])
            "a"))))
  (testing "Empty repo is merged into result"
    (is (= [{:id "a" :version 10 :location "loc_a"}]
           ((global-repo [(map-query {})
                            (map-query {"a" [{:id "a" :version 10 :location "loc_a"}]})])
            "a"))))
  (testing "All repositories are looked at"
    (is (= [{:id "a" :version 30 :location "loc_a30"}
            {:id "a" :version 20 :location "loc_a20"}
            {:id "a" :version 10 :location "loc_a10"}]
           ((global-repo [(map-query {})
                            (map-query {"a" [{:id "a" :version 10 :location "loc_a10"}]})
                            (map-query {"a" [{:id "a" :version 20 :location "loc_a20"}
                                             {:id "a" :version 30 :location "loc_a30"}]})])
            "a")))))
