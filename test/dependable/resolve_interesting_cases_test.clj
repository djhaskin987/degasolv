(ns dependable.resolve-interesting-cases-test
  (:require [clojure.test :refer :all]
            [dependable.resolver :refer :all]
            [clojure.core.match :refer [match]]
            [clj-semver.core :refer [cmp]])
  (:import [dependable.resolver
            package
            requirement]))

(deftest ^:resolve-interesting-cases managed-dependencies-case
  (let [a1
        {
         :id "a"
         :version "1.0.0"
         :location "http://example.com/repo/a-1.0.0.zip"
         :requirements [[{:status :present
                          :id "b"}]]
         }
        b23
        {
         :id "b"
         :version "2.3.0"
         :location "http://example.com/repo/b-2.3.0.zip"
         }
        b20
        {
         :id "b"
         :version "2.0.0"
         :location "http://example.com/repo/b-2.0.0.zip"
         }
        repo-info-asc {"a" [a1]
                   "b" [b20 b23]}
        query-asc (map-query repo-info-asc)
        repo-info-dsc {"a" [a1]
                       "b" [b23 b20]}
        query-dsc (map-query repo-info-dsc)]
    (testing "That managed dependencies are a thing"
      (is (= [:successful #{a1 b20}]
             (resolve-dependencies
              [[{:status :present
                 :id "a"}]
               [{:status :absent
                 :id "b"}
                {:status :present
                 :id "b"
                 :spec [[{:relation :less-than :version "2.2.0"}]]}]]
              query-dsc
              :compare cmp)))
      (is (= [:successful #{a1 b23}]
             (resolve-dependencies
              [[{:status :present
                 :id "a"}]
               [{:status :absent
                 :id "b"}
                {:status :present
                 :id "b"
                 :spec [[{:relation :greater-than :version "2.2.0"}]]}]]
              query-asc
              :compare cmp))))))
