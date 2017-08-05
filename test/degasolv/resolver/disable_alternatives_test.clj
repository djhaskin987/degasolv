(ns degasolv.resolver.disable-alternatives-test
  (:require [clojure.test :refer :all]
            [degasolv.resolver :refer :all]
            [clojure.core.match :refer [match]]
            [serovers.core :refer [maven-vercmp]
             :rename {maven-vercmp cmp}])
  (:import [degasolv.resolver
            PackageInfo
            Requirement]))

(deftest ^:unit-tests tutorial-test
         (let [repo-info
               {
                "a"
                [
                 {
                  :id "a"
                  :version "1.3.0"
                  :location "http://example.com/repo/a-1.3.0.zip"
                  :requirements
                  [
                   [
                    {:status :present
                     :id "d"
                     :spec [[{:relation :greater-equal :version "0.8.0"}]]}
                    {:status :present
                     :id "c"
                     :spec [[{:relation :greater-equal :version "3.5.0"}]]}
                    ]]}
                 ]
                "b"
                [
                 {
                  :id "b"
                  :version "2.3.0"
                  :location "http://example.com/repo/b-2.3.0.zip"
                  :requirements
                  [
                   [
                    {:status :present
                     :id "c"
                     :spec [[{:relation :greater-equal :version "3.5.0"}]]}
                    {:status :present
                     :id "d"
                     :spec [[{:relation :greater-equal :version "0.8.0"}]]}]]}
                 ]
                "c"
                [
                 {
                  :id "c"
                  :version "2.4.7"
                  :location "http://example.com/repo/c-2.4.7.zip"
                  :requirements []
                  }
                 ]
                "d"
                [
                 {
                  :id "d"
                  :version "0.8.0"
                  :location "http://example.com/repo/d-0.8.0.zip"
                  :requirements []
                  }
                 ]
                }
               query
               (map-query repo-info)]
           (testing "Basic test of denial of the use of alternatives."
                    (is (= :unsuccessful
                           (match
                           (resolve-dependencies
                             [[{:status :present :id "b" :spec [[{:relation :greater-than :version "2.0.0"}]]}]]
                             query
                             :compare cmp
                             :allow-alternatives false)
                           [:successful s]
                           (set (map :location s))
                           [:unsuccessful u]
                           :unsuccessful))))
           (testing "Test of success, showing the previous test means something."
                    (is (.equals #{
                                   "http://example.com/repo/a-1.3.0.zip"
                                   "http://example.com/repo/d-0.8.0.zip"
                                   }
                                 (match
                                   (resolve-dependencies
                                     [[{:status :present :id "a" :spec [[{:relation :greater-than :version "1.0.0"}]]}]]
                                     query
                                     :compare cmp
                                     :allow-alternatives false)
                                   [:successful s]
                                   (set (map :location s))
                                   [:unsuccessful u]
                                   :unsuccessful))))))
