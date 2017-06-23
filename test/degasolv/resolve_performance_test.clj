(ns degasolv.resolve-performance-test
  (:require [clojure.test :refer :all]
            [clojure.pprint :as pprint]
            [degasolv.resolver :refer :all]
            [clojure.core.match :refer [match]]
            [serovers.core :refer [maven-vercmp]
             :rename {maven-vercmp cmp}])
  (:import [degasolv.resolver
            PackageInfo
            Requirement]))


(def test-data (atom nil))

(defn reset-test-data
  "Resets test data before each test."
  [test-fn]
  (reset! test-data nil)
  (test-fn)
  (println "Test data:")
  (pprint/pprint @test-data))

(use-fixtures :each reset-test-data)
(deftest ^:resolve-performance pruning-candidates-test
  (let [repo-info
        {
         "a"
         [
          {
           :id "a"
           :version "1.2.0"
           :location "http://example.com/repo/a-1.2.0.zip"
           :requirements
           [[{:status :present
              :id "b"}]]}
          {
           :id "a"
           :version "1.1.0"
           :location "http://example.com/repo/a-1.1.0.zip"
           :requirements
           [[{:status :present
              :id "b"}]
            [{:status :present
              :id "c"}]]}]
         "b"
         [
          {
           :id "b"
           :version "2.3.0"
           :location "http://example.com/repo/b-2.3.0.zip"
           }
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
         }
        raw-query
        (map-query repo-info)
        sensing-query
        (fn [q]
          (swap!
           test-data
           assoc
           :repo-query-count
           (inc
            (:repo-query-count
             @test-data)))
          (raw-query q))]
    (swap! test-data assoc :repo-query-count 0)
    (testing "A test to see if repo is called too many times."
      (is (.equals #{
                 "http://example.com/repo/a-1.2.0.zip"
                 "http://example.com/repo/b-2.3.0.zip"
               }
          (match
           (resolve-dependencies
                           [[{:status :present :id "a" :spec [[{:relation :greater-than :version "1.0.0"}]]}]]
                           sensing-query
                           :compare cmp)
                    [:successful s]
                    (set (map :location s))
                    [:unsuccessful u]
                    :unsuccessful)))
      (is (= (:repo-query-count @test-data) 2)))))

(deftest ^:resolve-performance pruning-circular-test
  (let [repo-info
        {
         "a"
         [
          {
           :id "a"
           :version "1.2.0"
           :location "http://example.com/repo/a-1.2.0.zip"
           :requirements
           [[{:status :present
              :id "b"}]]}]
         "b"
         [
          {
           :id "b"
           :version "2.3.0"
           :location "http://example.com/repo/b-2.3.0.zip"
           :requirements
           [[{:status :present
              :id "a"}]]}]
         }
        raw-query
        (map-query repo-info)
        sensing-query
        (fn [q]
          (swap!
           test-data
           assoc
           :repo-query-count
           (inc
            (:repo-query-count
             @test-data)))
          (raw-query q))]
    (swap! test-data assoc :repo-query-count 0)
    (testing "A test to see if repo is called too many times."
      (is (.equals #{
                 "http://example.com/repo/a-1.2.0.zip"
                 "http://example.com/repo/b-2.3.0.zip"
               }
          (match
           (resolve-dependencies
                           [[{:status :present :id "a" :spec [[{:relation :greater-than :version "1.0.0"}]]}]]
                           sensing-query
                           :compare cmp)
                    [:successful s]
                    (set (map :location s))
                    [:unsuccessful u]
                    :unsuccessful)))
      (is (= (:repo-query-count @test-data) 2)))))

(deftest ^:resolve-performance pruning-circular-second-test
  (let [repo-info
        {
         "a"
         [
          {
           :id "a"
           :version "1.2.0"
           :location "http://example.com/repo/a-1.2.0.zip"
           :requirements
           [[{:status :present
              :id "a"}]]}]
         }
        raw-query
        (map-query repo-info)
        sensing-query
        (fn [q]
          (swap!
           test-data
           assoc
           :repo-query-count
           (inc
            (:repo-query-count
             @test-data)))
          (raw-query q))]
    (swap! test-data assoc :repo-query-count 0)
    (testing "A test to see if repo is called too many times."
      (is (.equals #{
                 "http://example.com/repo/a-1.2.0.zip"
               }
          (match
           (resolve-dependencies
                           [[{:status :present :id "a" :spec [[{:relation :greater-than :version "1.0.0"}]]}]]
                           sensing-query
                           :compare cmp)
                    [:successful s]
                    (set (map :location s))
                    [:unsuccessful u]
                    :unsuccessful)))
      (is (= (:repo-query-count @test-data) 1)))))


(deftest ^:resolve-performance pruning-alternatives-test
  (let [repo-info
        {
         "a"
         [
          {
           :id "a"
           :version "1.2.0"
           :location "http://example.com/repo/a-1.2.0.zip"
           :requirements
           [[
             {:status :present
              :id "c"}
             {:status :present
              :id "b"}
             ]]}]
         "b"
         [
          {
           :id "b"
           :version "2.3.0"
           :location "http://example.com/repo/b-2.3.0.zip"
           }
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
         }
        raw-query
        (map-query repo-info)
        sensing-query
        (fn [q]
          (swap!
           test-data
           assoc
           :repo-query-count
           (inc
            (:repo-query-count
             @test-data)))
          (raw-query q))]
    (swap! test-data assoc :repo-query-count 0)
    (testing "A test to see if repo is called too many times."
      (is (.equals #{
                 "http://example.com/repo/a-1.2.0.zip"
                 "http://example.com/repo/c-2.4.7.zip"
               }
          (match
           (resolve-dependencies
                           [[{:status :present :id "a" :spec [[{:relation :greater-than :version "1.0.0"}]]}]]
                           sensing-query
                           :compare cmp)
                    [:successful s]
                    (set (map :location s))
                    [:unsuccessful u]
                    :unsuccessful)))
      (is (= (:repo-query-count @test-data) 2)))))

(deftest ^:resolve-performance diamond-pruning-test
  (let [repo-info
        {
         "a"
         [
          {
           :id "a"
           :version "1.2.0"
           :location "http://example.com/repo/a-1.2.0.zip"
           :requirements
           [[{:status :present
              :id "b"}]
            [{:status :present
              :id "c"}]]}
          ]
         "b"
         [
          {
           :id "b"
           :version "2.3.0"
           :location "http://example.com/repo/b-2.3.0.zip"
           :requirements
           [[{:status :present
              :id "c"}]]
           }
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
         }
        raw-query
        (map-query repo-info)
        sensing-query
        (fn [q]
          (swap!
           test-data
           assoc
           :repo-query-count
           (inc
            (:repo-query-count
             @test-data)))
          (raw-query q))]
    (swap! test-data assoc :repo-query-count 0)
    (testing "A test to see if repo is called too many times."
      (is (.equals #{
                 "http://example.com/repo/a-1.2.0.zip"
                 "http://example.com/repo/c-2.4.7.zip"
                 "http://example.com/repo/b-2.3.0.zip"
               }
          (match
           (resolve-dependencies
                           [[{:status :present :id "a" :spec [[{:relation :greater-than :version "1.0.0"}]]}]]
                           sensing-query
                           :compare cmp)
                    [:successful s]
                    (set (map :location s))
                    [:unsuccessful u]
                    :unsuccessful)))
      (is (= (:repo-query-count @test-data) 3)))))
