(ns degasolv.resolve-data-spec-test
  (:require [clojure.test :refer :all]
            [degasolv.resolver :refer :all]
            [clojure.core.match :refer [match]]
            [version-clj.core :refer [version-compare]
             :rename {version-compare cmp}])
  (:import [degasolv.resolver
            PackageInfo
            Requirement]))

(deftest ^:resolve-data-spec tutorial-test
  (let [repo-info
        {
         "b"
         [
          {
           :id "b"
           :version "1.7.0"
           :location "http://example.com/repo/b-1.7.0.zip"
           :requirements []
           }
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
             ]
            [
             {:status :present
              :id "d"
              }
             ]
            ]
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
          {
           :id "c"
           :version "3.5.0"
           :requirements
           [[
             {:status :present
              :id "e"
              :spec [[{:relation :greater-equal :version "1.8.0"}]]}]]
           :location "http://example.com/repo/c-3.5.0.zip"
           }
          ]
         "d"
         [
          {
           :id "d"
           :version "0.8.0"
           :location "http://example.com/repo/d-0.8.0.zip"
           :requirements [[{:status :present :id "e"
                            :spec [[
                                    {:relation :greater-equal :version "1.1.0"}
                                    {:relation :less-than :version "2.0.0"}]]}]]
           }
          ]
         "e"
         [
          {
           :id "e"
           :version "2.4.0"
           :location "http://exmaple.com/repo/e-2.4.0.zip"
                                        ; A lack of requirements should work fine here,
                                        ; as this will yeild nil and work as an empty list.

           }
          {
           :id "e"
           :version "2.1.0"
           :location "http://exmaple.com/repo/e-2.1.0.zip"
           :requirements []
           }
          {
           :id "e"
           :version "1.8.0"
           :location "http://exmaple.com/repo/e-1.8.0.zip"
           :requirements []
           }
          ]
         }
        query
        (map-query repo-info)]
    (testing "A test of the tutorial."
      (is (= #{
                 "http://example.com/repo/b-2.3.0.zip"
                 "http://example.com/repo/c-3.5.0.zip"
                 "http://example.com/repo/d-0.8.0.zip"
                 "http://exmaple.com/repo/e-1.8.0.zip"
               }
          (match
           (resolve-dependencies
                           [[{:status :present :id "b" :spec [[{:relation :greater-than :version "2.0.0"}]]}]]
                           query
                           :compare cmp)
                    [:successful s]
                    (set (map :location s))
                    [:unsuccessful u]
                    :unsuccessful))))))

(deftest ^:resolve-data-spec data-spec-cases
  (let [b1
        {
         :id "b"
         :version "1.0.0"
         :location "http://example.com/repo/b-1.0.0.zip"
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
        repo-info-mixed {"b" [b1 b23 b20]}
        query-mixed (map-query repo-info-mixed)
        repo-info-desc {"b" [b23 b20 b1]}
        query-desc (map-query repo-info-desc)
        repo-info-asc {"b" [b1 b20 b23]}
        query-asc (map-query repo-info-asc)]
    (testing "greater-than data spec case"
      (is (= [:successful #{b23}]
             (resolve-dependencies
              [[{:status :present
                 :id "b"
                 :spec [[{:relation :greater-than :version "2.0.0"}]]}]]
              query-asc
              :compare cmp))))

    (testing "greater-equal, less-than data spec case"
      (is (= [:successful #{b20}]
             (resolve-dependencies
              [[{:status :present
                 :id "b"
                 :spec [[{:relation :greater-equal :version "2.0.0"}
                         {:relation :less-than :version "2.3.0"}]]}]]
              query-mixed
              :compare cmp))))
    (testing "less-equal data spec case"
      (is (= [:successful #{b23}]
             (resolve-dependencies
              [[{:status :present
                 :id "b"
                 :spec [[{:relation :less-equal :version "2.3.0"}
                         {:relation :greater-than :version "2.0.0"}]]}]]
              query-asc
              :compare cmp))))
    (testing "equal-to data spec case"
      (is (= [:successful #{b20}]
             (resolve-dependencies
              [[{:status :present
                 :id "b"
                 :spec [[{:relation :equal-to :version "2.0.0"}]]}]]
              query-mixed
              :compare cmp))))
    (testing "not-equal data spec case"
      (is (= [:successful #{b1}]
             (resolve-dependencies
              [[{:status :present
                 :id "b"
                 :spec [[{:relation :not-equal :version "2.3.0"}
                         {:relation :not-equal :version "2.0.0"}]]}]]
              query-desc
              :compare cmp))))
    (testing "dual ranges spec cases"
      (is (= [:successful #{b23}]
             (resolve-dependencies
              [[{:status :present
                 :id "b"
                 :spec [[{:relation :greater-than :version "2.0.0"}]
                        [{:relation :less-than :version "1.7.0"}]]}]]
              query-desc
              :compare cmp)))
      (is (= [:successful #{b1}]
             (resolve-dependencies
              [[{:status :present
                 :id "b"
                 :spec [[{:relation :greater-than :version "2.0.0"}]
                        [{:relation :less-than :version "1.7.0"}]]}]]
              query-asc
              :compare cmp))))))

