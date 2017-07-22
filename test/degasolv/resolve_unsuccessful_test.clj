(ns degasolv.resolve-unsuccessful-test
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
(deftest ^:unit-test unsuccessful-test
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
        c3
        {
         :id "c"
         :version "3.0.0"
         :location "http://example.com/repo/c-3.0.0.zip"
         :requirements
         [
          [
           d-clause
           ]
          ]
         }
        repo-info
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
        query
        (map-query repo-info)]
    (testing "Multiple unsuccessfuls should be returned across alternatives"
      (is (=
           [:unsuccessful
            {:problems
             [
              {:term d-clause
               :alternatives d-alternative
               :found-packages [a1
                                b2]
               :present-packages nil
               :absent-specs nil
               :reason :package-rejected
               :package-id "d"}
              {:term d-clause
               :alternatives d-alternative
               :found-packages [a1
                                c3]
               :present-packages nil
               :absent-specs nil
               :reason :package-rejected
               :package-id "d"}]}]

           (resolve-dependencies
            [[{:status :present :id "a"}]]
            query
            :compare cmp))))))
