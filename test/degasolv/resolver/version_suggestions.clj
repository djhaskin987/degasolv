(ns degasolv.resolver.version-suggestions
  (:require [clojure.test :refer :all]
            [degasolv.resolver :refer :all]))

(deftest
  ^:unit-tests minimum-version-selection-case
  (let [a
        {
         :id "a"
         :version 10
         :location "http://example.com/repo/a-1.0.0.zip"
         :requirements [
                        [(present "c" #(>= (:version %) 10))]
                        [(present "b")]
                        ]
         }
        b
        {
         :id "b"
         :version 10
         :location "http://example.com/repo/b-1.0.0.zip"
         :requirements [
                        [(present "c" #(>= (:version %) 30))]
                        ]

         }
        c10
        {
         :id "c"
         :version 10
         :location "http://example.com/repo/c-1.0.0.zip"
         }
        c20
        {
         :id "c"
         :version 20
         :location "http://example.com/repo/c-2.0.0.zip"
         :requirements [[(present "d" #(do (print "foo") (throw (ex-info
                                  "This bomb is intentional. Any attempt to resolve this candidate should cause an error."
                                  {:value %}))))]]
         }
        c30
        {
         :id "c"
         :version 30
         :location "http://example.com/repo/c-3.0.0.zip"
         }
        c40
        {
         :id "c"
         :version 40
         :location "http://example.com/repo/c-4.0.0.zip"
         }
        d
        {
         :id "d"
         :version 10
         :location "http://example.com/repo/d-1.0.0.zip"
         }
        repo-info-asc {"a" [a]
                       "b" [b]
                       "c" [c10 c20 c30 c40]
                       "d" [d]}
        query-asc (map-query repo-info-asc)]
  (deftest
    ^:unit-tests
    ^:minimum-version-selection
    ^:version-suggestion
    mvs
    (testing
      "Make sure that the bomb version (c20) is skipped."
      (is (.equals [:successful #{a b c30}]
                   (resolve-dependencies
                    [
                     [(present "a")]
                     ]
                    query-asc)))))))
