(ns degasolv.resolver.unsuccessful-test
  (:require [clojure.test :refer :all]
            [degasolv.resolver :refer :all]
            [clojure.core.match :refer [match]]
            [serovers.core :refer [maven-vercmp]
             :rename {maven-vercmp cmp}])
  (:import [degasolv.resolver
            PackageInfo
            Requirement]))
;; todo
;; - empty fclause
;; - present package conflict
(deftest ^:unit-tests unsuccessful-test
  (let [
        b-alternative
        {:status :present
         :id "b"
         }
        c-alternative
        {:status :present
          :id "c"
         }
        d-alternative
        {:status :present
         :id "d"
         }
        d-clause
        [
         d-alternative
         ]
        altclause
        [
         b-alternative
         c-alternative
         ]
        bclause
        [
         b-alternative
         ]
        a1
        {
         :id "a"
         :version "1.0.0"
         :location "http://example.com/repo/a-1.0.0.zip"
         :requirements
         [
          altclause
          ]
         }
        a2
        {
         :id "a"
         :version "2.0.0"
         :location "http://example.com/repo/a-2.0.0.zip"
         :requirements
         [
          bclause
          ]
         }
        a3
        {
         :id "a"
         :version "2.0.0"
         :location "http://example.com/repo/a-2.0.0.zip"
         :requirements
         [
          []
          ]
         }
        b2
        {
         :id "b"
         :version "2.0.0"
         :location "http://example.com/repo/b-2.0.0.zip"
         :requirements
         [
          d-clause
          ]
         }
        b3
        {
         :id "b"
         :version "3.0.0"
         :location "http://example.com/repo/b-3.0.0.zip"
         :requirements
         [
          d-clause
          ]
         }
        c3
        {
         :id "c"
         :version "3.0.0"
         :location "http://example.com/repo/c-3.0.0.zip"
         :requirements
         [
          d-clause
          ]
         }
        both-alts-bad-repo-info
        {
         "a"
         [
          a1
          ]
         "b"
         [
          b2
          ]
         "c"
         [
          c3
          ]
         }
        both-bs-bad-repo-info
        {
         "a"
         [
          a2
          ]
         "b"
         [
          b2
          b3
          ]
         }
        adeps-bad-repo-info
        {
         "a"
         [
          a3
          ]
         }
        alts-bad-query
        (map-query both-alts-bad-repo-info)
        bs-bad-query
        (map-query both-bs-bad-repo-info)
        adeps-bad-query
        (map-query adeps-bad-repo-info)
        ]
    (testing "Multiple unsuccessfuls should be returned across alternatives"
      (is (=
           [:unsuccessful
            {:problems
             [
              {:term d-clause
               :alternative d-alternative
               :found-packages {"a"
                                [a1]
                                "b"
                                [b2]
                                }
               :present-packages {}
               :absent-specs {}
               :reason :package-not-found
               :package-id "d"}
              {:term d-clause
               :alternative d-alternative
               :found-packages {
                                "a"
                                [a1]
                                "c"
                                [c3]}
               :present-packages {}
               :absent-specs {}
               :reason :package-not-found
               :package-id "d"}]}]

           (resolve-dependencies
            [[{:status :present :id "a"}]]
            alts-bad-query
            :compare cmp))))
    (testing "Multiple unsuccessfuls should be returned across candidates"
      (is (=
           [:unsuccessful
            {:problems
             [
              {:term d-clause
               :alternative d-alternative
               :found-packages {"a"
                                [a2]
                                "b"
                                [b3]}
               :present-packages {}
               :absent-specs {}
               :reason :package-not-found
               :package-id "d"}
              {:term d-clause
               :alternative d-alternative
               :found-packages {"a"
                                [a2]
                                "b"
                                [b2]}
               :present-packages {}
               :absent-specs {}
               :reason :package-not-found
               :package-id "d"}]}]
           (resolve-dependencies
            [[{:status :present :id "a"}]]
            bs-bad-query
            :compare cmp))))
    (testing "Empty alternatives should be unsuccessful"
      (is (=
           [:unsuccessful
            {:problems
             [{:term []
               :found-packages {
                                "a" [a3]
                                }
               :present-packages {}
               :absent-specs {}
               :reason :empty-alternative-set}]}]
           (resolve-dependencies
            [[{:status :present :id "a"}]]
            adeps-bad-query
            :compare cmp))))))
