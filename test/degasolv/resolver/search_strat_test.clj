(ns degasolv.resolver.search-strat-test
  (:require [clojure.test :refer :all]
            [degasolv.resolver :refer :all]
            [clojure.core.match :refer [match]]
            [serovers.core :refer [maven-vercmp]
             :rename {maven-vercmp cmp}])
  (:import [degasolv.resolver
            PackageInfo
            Requirement]))

(deftest ^:unit-tests basic-dfs
  (testing "Make sure ``the puzzle`` still works"
    (let [package-a
          (->package
           "a"
           1
           "a_loc1"
           [
            [(present "b")]
            [(present "c")]
            ]
           )
          package-b
          (->package
           "b"
           1
           "b_loc1"
           [
            [(present "d" #(>= (:version %) 1))]
            ]
           )
          package-c
          (->package
           "c"
           1
           "c_loc1"
           [
            [(present "d" #(< (:version %) 4))]
            ]
           )
          package-d1
          (->package
           "d"
           1
           "d_loc1"
           [
            [(present "e" #(.equals (:version %) 4))]
            ]
           )
          package-d2
          (->package
           "d"
           2
           "d_loc2"
           [
            [(present "e" #(.equals (:version %) 3))]
            ]
           )
          package-e4
          (->package
           "e"
           4
           "e_loc4"
           nil)
          package-e3
          (->package
           "3"
           3
           "e_loc3"
           nil)
          repo-info
          {"a" [package-a]
           "b" [package-b]
           "c" [package-c]
           "d" [package-d2 package-d1]
           "e" [package-e4 package-e3]}
          query
          (map-query repo-info)]
      (is (.equals [:successful #{package-a
                                  package-b
                                  package-c
                                  package-d2
                                  package-e3}]
                   (resolve-dependencies
                    [
                     [(present "a")]
                     ]
                    query
                    :search-strat :depth-first)))))
  (let [d
        {
         :id "d"
         :version "4.0.0"
         :location "http://example.com/repo/d-4.0.0.zip"
         }
        c
        {
         :id "c"
         :version "2.7.0"
         :location "http://example.com/repo/c-2.7.0.zip"
         }
        b
        {
         :id "b"
         :version "1.0.0"
         :location "http://example.com/repo/b-1.0.0.zip"
         :requirements
         [
          [
           {:status :present
            :id "d"
            }
           {:status :present
            :id "c"
            }
           ]
          ]
         }
        a
        {
         :id "a"
         :version "1.0.0"
         :location "http://example.com/repo/a-1.0.0.zip"
         :requirements
         [
          [
           {:status :present
            :id "b"
            }
           ]
          [
           {:status :present
            :id "c"
            }
           {:status :present
            :id "d"
            }
           ]
          ]
         }
        repo-info
        {
         "a"
         [
          a
          ]
         "b"
         [
          b
          ]
         "c"
         [
          c
          ]
         "d"
         [
          d
          ]
         }
        query (map-query repo-info)]
    (testing "order of resolution depending on search-strat - breadth first"
      (is (= [:successful
              #{c
                b
                a}]
             (resolve-dependencies
              [[{:status :present :id "a"}]]
              query
              :search-strat :breadth-first
              :compare cmp))))
    (testing "order of resolution depending on search-strat - depth first"
      (is (= [:successful
              #{d
                b
                a}]
             (resolve-dependencies
              [[{:status :present :id "a"}]]
              query
              :search-strat :depth-first
              :compare cmp)))))
  (let [e
        {
         :id "e"
         :version "7.0.0"
         :location "http://example.com/repo/e-7.0.0.zip"
         }
        d
        {
         :id "d"
         :version "4.0.0"
         :location "http://example.com/repo/d-4.0.0.zip"
         }
        c
        {
         :id "c"
         :version "2.7.0"
         :location "http://example.com/repo/c-2.7.0.zip"
         }
        b
        {
         :id "b"
         :version "1.0.0"
         :location "http://example.com/repo/b-1.0.0.zip"
         :requirements
         [
          [
           {:status :absent
            :id "d"
            }
           {:status :present
            :id "e"
            }
           ]
          ]
         }
        a
        {
         :id "a"
         :version "1.0.0"
         :location "http://example.com/repo/a-1.0.0.zip"
         :requirements
         [
          [
           {:status :present
            :id "b"
            }
           ]
          [
           {:status :present
            :id "d"
            }
           {:status :present
            :id "c"
            }
           ]
          ]
         }
        repo-info
        {
         "a"
         [
          a
          ]
         "b"
         [
          b
          ]
         "c"
         [
          c
          ]
         "d"
         [
          d
          ]
         "e"
         [
          e
          ]
         }
        query (map-query repo-info)]
    (testing "search strat: order of resolution: breadth first: absent"
      (is (= [:successful
              #{a
                b
                d
                e}]
             (resolve-dependencies
              [[{:status :present :id "a"}]]
              query
              :search-strat :breadth-first
              :compare cmp))))
    (testing "search strat: order of resolution: depth first: absent"
      (is (= [:successful
              #{a
                b
                c}]
             (resolve-dependencies
              [[{:status :present :id "a"}]]
              query
              :search-strat :depth-first
              :compare cmp)))))
  (let [c40
        {
         :id "c"
         :version "4.0.0"
         :location "http://example.com/repo/c-4.0.0.zip"
         }
        c27
        {
         :id "c"
         :version "2.7.0"
         :location "http://example.com/repo/c-2.7.0.zip"
         }
        b
        {
         :id "b"
         :version "1.0.0"
         :location "http://example.com/repo/b-1.0.0.zip"
         :requirements
         [
          [
           {:status :present
            :id "c"
            :spec [[{:relation :equal-to :version "2.7.0"}]]
            }
           ]
          ]
         }
        a
        {
         :id "a"
         :version "1.0.0"
         :location "http://example.com/repo/a-1.0.0.zip"
         :requirements
         [
          [
           {:status :present
            :id "b"
            }
           ]
          [
           {:status :present
            :id "c"
            :spec [[{:version "4.0.0" :relation :equal-to}]]
            }
           ]
          ]
         }
        repo-info
        {
         "a"
         [
          a
          ]
         "b"
         [
          b
          ]
         "c"
         [
          c40
          c27
          ]
         }
        query (map-query repo-info)]
    (testing "breadth-first conflict-strat as prioritized"
      (is (= [:successful #{a b c40}]
             (resolve-dependencies
              [[{:status :present :id "a"}]]
              query
              :conflict-strat :prioritized
              :search-strat :breadth-first
              :compare cmp))))
    (testing "depth-first conflict-strat as prioritized"
      (is (= [:successful #{a b c27}]
             (resolve-dependencies
              [[{:status :present :id "a"}]]
              query
              :conflict-strat :prioritized
              :search-strat :depth-first
              :compare cmp))))))
