(ns degasolv.resolve-conflict-strat-test
  (:require [clojure.test :refer :all]
            [degasolv.resolver :refer :all]
            [clojure.core.match :refer [match]]
            [version-clj.core :refer [version-compare]
             :rename {version-compare cmp}])
  (:import [degasolv.resolver
            PackageInfo
            Requirement]))

(deftest ^:resolve-conflict-strat conflict-strats-simple
  (let [repo-info
        {
         "a"
         [
          {
           :id "a"
           :version "1.0"
           :location "http://example.com/repo/a-1.0.zip"
           :requirements
           [
            [
             {:status :present
              :id "b"
              :spec [[{:relation :equal-to :version "0.6.0"}]]
              }
             ]
            [
             {:status :present
              :id "c"
              :spec [[{:relation :equal-to :version "0.2.3"}]]}
             ]
            ]
           }]
         "b"
         [
          {
           :id "b"
           :version "0.5.0"
           :location "http://example.com/repo/b-0.5.0.zip"
           :requirements []
           }
          {
           :id "b"
           :version "0.6.0"
           :location "http://example.com/repo/b-0.6.0.zip"
           :requirements []
           }
          ]
         "c"
         [
          {
           :id "c"
           :version "0.2.3"
           :location "http://example.com/repo/c-0.2.3.zip"
           :requirements
           [
            [
             {:status :present
              :id "b"
              :spec [[{:relation :equal-to :version "0.5.0"}]]}
             ]
            ]
           }
          ]
         }
        query
        (map-query repo-info)]

    (testing "A simple test of the inclusive conflict strat."
      (is (.equals #{
                 "http://example.com/repo/a-1.0.zip"
                 "http://example.com/repo/b-0.6.0.zip"
                 "http://example.com/repo/b-0.5.0.zip"
                 "http://example.com/repo/c-0.2.3.zip"
               }
          (match
           (resolve-dependencies
                           [[{:status :present :id "a"}]]
                           query
                           :compare cmp
                           :conflict-strat :inclusive)
                    [:successful s]
                    (set (map :location s))
                    [:unsuccessful u]
                    :unsuccessful))))
    (testing "A test of the prioritized conflict strat."
      (is (.equals #{
                 "http://example.com/repo/a-1.0.zip"
                 "http://example.com/repo/b-0.6.0.zip"
                 "http://example.com/repo/c-0.2.3.zip"
               }
          (match
           (dbg2 (resolve-dependencies
                           [[{:status :present :id "a"}]]
                           query
                           :compare cmp
                           :conflict-strat :prioritized))
                    [:successful s]
                    (set (map :location s))
                    [:unsuccessful u]
                    :unsuccessful))))))

;; TODO: Test empty cases
;; TODO: Test that it implies `fast`
;; TODO: Test using absences
;; TODO: Remove present-packages from the arg list
