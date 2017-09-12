(ns degasolv.resolver.data-spec-test
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
      (is (.equals #{
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

(deftest ^:unit-tests range-spec-cases
  (let [b3
        {
         :id "b"
         :version "3.0.0"
         :location "http://example.com/repo/b-3.0.0.zip"
         }
        b35
        {
         :id "b"
         :version "3.5.0"
         :location "http://example.com/repo/b-3.5.0.zip"
         }
        b4
        {
         :id "b"
         :version "4.0.0"
         :location "http://example.com/repo/b-4.0.0.zip"
         }
        repo-info-asc {"b" [b3 b35 b4]}
        query-asc (map-query repo-info-asc)
        repo-info-desc {"b" [b4 b35 b3]}
        query-desc (map-query repo-info-desc)]
    (testing "range start inclusive"
      (is (.equals [:successful #{b3}]
             (resolve-dependencies
              [[{:status :present
                 :id "b"
                 :spec [[{:relation :in-range :version "3.x"}]]}]]
              query-asc
              :compare cmp))))
    (testing "range end exclusive"
      (is (.equals [:successful #{b35}]
                   (resolve-dependencies
                    [[{:status :present
                       :id "b"
                       :spec [[{:relation :in-range :version "3.x"}]]}]]
                    query-desc
                    :compare cmp))))
    (testing "sub range"
      (is (.equals [:successful #{b35}]
             (resolve-dependencies
              [[{:status :present
                 :id "b"
                 :spec [[{:relation :in-range :version "3.5.x"}]]}]]
              query-asc
              :compare cmp))))))

(deftest ^:unit-tests data-spec-cases
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
        b31
        {
         :id "b"
         :version "3.1.0"
         :location "http://example.com/repo/b-3.1.0.zip"
         }
        repo-info-mixed {"b" [b1 b23 b20 b31]}
        query-mixed (map-query repo-info-mixed)
        repo-info-desc {"b" [b31 b23 b20 b1]}
        query-desc (map-query repo-info-desc)
        repo-info-asc {"b" [b1 b20 b23 b31]}
        query-asc (map-query repo-info-asc)]
    (testing "greater-than data spec case"
      (is (.equals [:successful #{b23}]
             (resolve-dependencies
              [[{:status :present
                 :id "b"
                 :spec [[{:relation :greater-than :version "2.0.0"}]]}]]
              query-asc
              :compare cmp))))

    (testing "greater-equal, less-than data spec case"
      (is (.equals [:successful #{b20}]
             (resolve-dependencies
              [[{:status :present
                 :id "b"
                 :spec [[{:relation :greater-equal :version "2.0.0"}
                         {:relation :less-than :version "2.3.0"}]]}]]
              query-mixed
              :compare cmp))))
    (testing "less-equal data spec case"
      (is (.equals [:successful #{b23}]
             (resolve-dependencies
              [[{:status :present
                 :id "b"
                 :spec [[{:relation :less-equal :version "2.3.0"}
                         {:relation :greater-than :version "2.0.0"}]]}]]
              query-asc
              :compare cmp))))
    (testing "equal-to data spec case"
      (is (.equals [:successful #{b20}]
             (resolve-dependencies
              [[{:status :present
                 :id "b"
                 :spec [[{:relation :equal-to :version "2.0.0"}]]}]]
              query-mixed
              :compare cmp))))
    (testing "not-equal data spec case"
      (is (.equals [:successful #{b1}]
             (resolve-dependencies
              [[{:status :present
                 :id "b"
                 :spec [[{:relation :not-equal :version "3.1.0"}
                         {:relation :not-equal :version "2.3.0"}
                         {:relation :not-equal :version "2.0.0"}]]}]]
              query-desc
              :compare cmp))))
    (testing "ranges spec case 1"
      (is (.equals [:successful #{b1}]
                   (resolve-dependencies
                    [[{:status :present
                       :id "b" :spec [[{:relation :in-range :version "01"}]]}]]
                    query-desc
                    :compare cmp))))
    (testing "ranges spec case 2"
      (is (.equals [:successful #{b23}]
                   (resolve-dependencies
                    [[{:status :present
                       :id "b" :spec [[{:relation :in-range :version "2.003"}]]}]]
                    query-asc
                    :compare cmp))))
    (testing "ranges spec case 3"
      (is (.equals [:successful #{b23}]
                   (resolve-dependencies
                    [[{:status :present
                       :id "b" :spec [[{:relation :in-range :version "2.3.x"}]]}]]
                    query-asc
                    :compare cmp))))
    (testing "pessimistic greater spec case 1"
      (is (= [:successful #{b1}]
                   (resolve-dependencies
                    [[{:status :present
                       :id "b" :spec [[{:relation :pess-greater :version "1.0.0"}]]}]]
                    query-desc
                    :compare cmp))))
    (testing "pessimistic greater spec case 2"
      (is (.equals [:successful #{b20}]
                   (resolve-dependencies
                    [[{:status :present
                       :id "b" :spec [[{:relation :pess-greater :version "2"}]]}]]
                    query-asc
                    :compare cmp))))
    (testing "pessimistic greater spec case 3"
      (is (.equals [:successful #{b23}]
                   (resolve-dependencies
                    [[{:status :present
                       :id "b" :spec [[{:relation :pess-greater :version "2.3.0"}]]}]]
                    query-asc
                    :compare cmp))))
    (testing "pessimistic greater spec case 4"
      (is (.equals [:successful #{b31}]
                   (resolve-dependencies
                    [[{:status :present
                       :id "b" :spec [[{:relation :pess-greater :version "3.0.0"}]]}]]
                    query-asc
                    :compare cmp))))
    (testing "regex case"
      (is (.equals [:successful #{b31}]
                   (resolve-dependencies
                    [[{:status :present
                       :id "b" :spec [[{:relation :matches :version "^[0-9][.][0-9][.][0-9]"}]]}]]
                    query-desc
                    :compare cmp))))
    (testing "bad regex case"
      (let [result (resolve-dependencies
                    [[{:status :present
                       :id "b" :spec [[{:relation :matches :version "^["}]]}]]
                    query-desc
                    :compare cmp)]
        (is (= :unsuccessful (first result)))))
    (testing "dual ranges spec cases"
      (is (.equals [:successful #{b31}]
             (resolve-dependencies
              [[{:status :present
                 :id "b"
                 :spec [[{:relation :greater-than :version "2.0.0"}]
                        [{:relation :less-than :version "1.7.0"}]]}]]
              query-desc
              :compare cmp)))
      (is (.equals [:successful #{b1}]
             (resolve-dependencies
              [[{:status :present
                 :id "b"
                 :spec [[{:relation :greater-than :version "2.0.0"}]
                        [{:relation :less-than :version "1.7.0"}]]}]]
              query-asc
              :compare cmp))))))
