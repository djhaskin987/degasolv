(ns dependable.resolve-test
  (:require [clojure.test :refer :all]
            [dependable.resolver :refer :all]
            [clj-semver.core :refer [older?]])
  (:import [dependable.resolver
            package
            requirement]))

(defn map-query [m]
  (fn [nm]
    (let [result (find m nm)]
      (if (nil? result)
        []
        (let [[k v] result]
          v)))))
;; TODO: disjunctive clause tests

(let  [package-a30
       (->package
        "a"
        30
        "a_loc30"
        [
         [(present "c")]
         ])
       package-a20
       (->package
        "a"
        20
        "a_loc20"
        nil)
       package-c10
       (->package
        "c"
        10
        "c_loc10"
        nil)
       package-d22
       (->package
        "d"
        22
        "d_loc22"
        nil)
       package-e18
       (->package
        "e"
        18
        "e_loc18"
        [
         [(absent "d")]
         ])
       repo-info
       {"a"
        [package-a30
         package-a20]
        "c"
        [package-c10]
        "d"
        [package-d22]}
       query (map-query repo-info)]
  (deftest ^:resolve-basic ^:resolve-first-tier retrieval
           (testing
             "Asking for a present package succeeds."
             (is (= [:successful #{package-d22}]
                    (resolve-dependencies
                      [
                       [(present "d")]
                       ]
                      query)))
             (is (= [:successful #{package-c10}]
                    (resolve-dependencies
                      [
                       [(present "c")]
                       ]
                      query))))
           (testing
             "Asking for a nonexistent package fails."
             (let [b-clause [(present "b")]]
               (is (= [:unsuccessful b-clause]
                      (resolve-dependencies
                        [b-clause]
                        query)))))
           (testing
             (str "Asking for a package present within the repo but "
                  "at no suitable version fails.")
             (let [a-clause
                   [(present
                      "a"
                      #(>= (:version %1) 40))]]
               (is (= [:unsuccessful a-clause]
                      (resolve-dependencies
                        [a-clause]
                        query)))))
           (testing
             (str "Asking for a package present within the repo but "
                  "with unsuccessful constraints.")
             (let [a-clause
                   [(present
                      "a"
                      (fn [v] false))]]
               (is (= [:unsuccessful a-clause]
                      (resolve-dependencies
                        [a-clause]
                        query)))))
           (testing
             (str "Asking for a package present and having a "
                  "version that fits")
             (is (= [:successful #{package-a20}]
                    (resolve-dependencies
                      [[(present "a" #(and (>= (:version %1) 15)
                                           (<= (:version %1) 25)))]]
                      query)))
             (is (= [:successful #{package-d22}]
                    (resolve-dependencies
                      [[(present "d" #(>= (:version %1) 20))]]
                      query)))))
  (deftest ^:resolve-basic present-packages
           (testing
             "Asking to install a package twice."
             (is (= [:successful #{package-c10}]
                    (resolve-dependencies
                      [
                       [(present "c")]
                       [(present "c")]
                       ]
                      query))))
           (testing
             (str "Asking to install a package that I have given as "
                  "already installed.")
             (is (= [:successful #{}]
                    (resolve-dependencies
                      [
                       [(present "c")]
                       ]
                      query
                      :present-packages
                      {"c" package-c10}))))

           (testing
             (str "Asking to install a package that I have given as already "
                  "installed, even though the package isn't available.")
             (is (= [:successful #{}]
                    (resolve-dependencies
                      [
                       [(present "b")]
                       ]
                      query
                      :present-packages {"b"
                                         (->package "b" 10 "b-loc10" nil)}))))
           (testing
             (str "Asking to install a package that is already "
                  "installed, but the installed version doesn't "
                  "suit, even though there is a suitable version "
                  "available.")
             (let [clause [(present "a" #(>= (:version %) 25))]]
               (is (= [:unsuccessful clause]
                      (resolve-dependencies
                        [
                         clause
                         ]
                        query
                        :present-packages {"a" package-a20})))))

           (testing
             (str "Asking to install a package that is already "
                  "installed, and the installed version suits.")
             (is (= [:successful #{}]
                    (resolve-dependencies
                      [
                       [(present "a" #(>= (:version %) 20))]
                       ]
                      query
                      :present-packages {"a" package-a20})))))
  (deftest ^:resolve-basic conflicts
           (let [dclause [(present "d")]]
             (testing
               (str "Find a package which conflicts with another "
                           "package also to be installed.")
               (is (= [:unsuccessful dclause]
                      (resolve-dependencies
                        [dclause
                         [(present "e")]]
                        query))))
             (testing (str "Find a package which conflicts with a package "
                           "marked a priori as conflicting.")
                      (is (= [:unsuccessful dclause]
                             (resolve-dependencies
                               [dclause
                                [(present "a" #(<= (:version %) 25))]]
                               query
                               :conflicts {"d" [nil]}))))
             (testing (str "Find a package which conflicts with another "
                           "package but not at its current version")
                      (is (= [:successful #{package-d22}]
                             (resolve-dependencies
                               [dclause]
                               query
                               :conflicts {"d" [#(< (:version %) 22)]})))))))

(deftest ^:resolve-basic requires
    (let [package-a
          (->package
            "a"
            30
            "a_loc30"
            [
             [(present "b")]
             ])
          package-b
          (->package
            "b"
            20
            "b_loc20"
            nil)
          repo-info
          {"a" [package-a]
           "b" [package-b]}
          query (map-query repo-info)]
      (testing
        (str "One package should require another and both "
             "should be found.")
        (is (= [:successful #{package-a package-b}]
               (resolve-dependencies
                 [
                  [(present "a")]
                  ]
                 query))))
      (testing
        (str "One package should be found when it requires "
             "another, but it's already installed.")
        (is (= [:successful #{package-a}]
               (resolve-dependencies
                 [
                  [(present "a")]
                  ]
                 query
                 :present-packages {"b" package-b})))))
    (let [package-a
          (->package
            "a"
            10
            "a_loc10"
            nil)
          package-b
          (->package
            "b"
            20
            "b_loc20"
            [
             [(present "c")]
             ])
          package-c
          (->package
            "c"
            10
            "c_loc10"
            [
             [(absent "a")]
             ])
          repo-info
          {"a" [package-a]
           "b" [package-b]
           "c" [package-c]}
          query (map-query repo-info)]
      (testing
        (str "A package having dependencies which conflicts with "
             "other packages downloaded should be rejected.")

        (let [aclause [(present "a")]
              bclause [(present "b")]]
          (is (= [:unsuccessful aclause]
                 (resolve-dependencies
                   [
                    aclause
                    bclause
                    ]
                   query)))))
    (let [package-a
          (->package
            "a"
            10
            "a_loc10"
            nil)
          package-b
          (->package
            "b"
            10
            "b_loc10"
            [
             [(present "c")]
             ]
            )
          package-c
          (->package
            "c"
            10
            "c_loc10"
            nil
            )
          repo-info
          {"a" [package-a]
           "b" [package-b]
           "c" [package-c]}
          query (map-query repo-info)]
      (testing
        (str "A package having dependencies rejected a priori "
             "also gets rejected")
        (let [aclause [(present "a")]]

          (is (= [:unsuccessful aclause]
                 (resolve-dependencies
                   [
                    aclause
                    [(present "b")]
                    ]
                   query
                   :conflicts {"c" [nil]}))
              ))))))

(deftest
  ^:resolve-basic disjunctive-clauses
  (testing
    "Disjunction tautology"
    (is (= [:successful #{}]
           (resolve-dependencies
             [
              [(absent "c") (present "b")]
              ]
             (map-query {})))))
  (testing
    "Skip past a conflict"
    (let [package-a
          (->package
            "a"
            30
            "a_loc30"
            [
             [(present "b")]
             ])
          package-b
          (->package
            "b"
            20
            "b_loc20"
            nil)
          repo-info
          {"a" [package-a]
           "b" [package-b]}
          query (map-query repo-info)]
      (is (= [:successful #{package-b}]
             (resolve-dependencies
               [
                [(absent "c") (present "b")]
                ]
               query
               :present-packages {"c" (->package "c" 10 "c_loc10" nil)}))))))

(deftest ^:resolve-basic no-locking
    (testing
      (str "Find two packages, even when the preferred version "
           "of one package conflicts with the other")
      (let [package-a30
            (->package
              "a"
              30
              "a_loc30"
              [
               [(absent "c")]
               ])
            package-a20
            (->package
              "a"
              20
              "a_loc20"
              nil)
            package-c10
            (->package
              "c"
              10
              "c_loc10"
              nil)
            repo-info
            {"a" [package-a30 package-a20]
             "c" [package-c10]}
            query (map-query repo-info)]
        (is (= [:successful #{package-a20 package-c10}]
               (resolve-dependencies
                 [
                  [(present "a")]
                  [(present "c")]
                  ]
                 query)))))
    (testing (str "Diamond problem")
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
               [(present
                  "d"
                  #(>= (:version %) 2))]
               ]
              )
            package-c
            (->package
              "c"
              1
              "c_loc1"
              [
               [(present
                  "d"
                  #(< (:version %) 4))]
               ]
              )
            package-d3
            (->package
              "d"
              3
              "d_loc3"
              nil)
            package-d4
            (->package
              "d"
              4
              "d_loc4"
              nil)
            repo-info
            {"a" [package-a]
             "b" [package-b]
             "c" [package-c]
             "d" [package-d4 package-d3]}
            query
            (map-query repo-info)]
        (is (= [:successful
                #{package-a
                  package-b
                  package-c
                  package-d3}]
               (resolve-dependencies
                 [[(present "a")]]
                 query)))))
    (testing (str "Inter-Locking Diamond problem")
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
               [(present "d" #(>= (:version %) 2))]
               [(present "e" #(= (:version %) 5))]
               ]
              )
            package-c
            (->package
              "c"
              1
              "c_loc1"
              [
               [(present "e" #(>= (:version %) 1))]
               [(present "d" #(< (:version %) 4))]
               ]
              )
            package-d3
            (->package
              "d"
              3
              "d_loc3"
              nil)
            package-d4
            (->package
              "d"
              4
              "d_loc4"
              nil)
            package-e6
            (->package
              "e"
              6
              "e_loc6"
              nil)
            package-e5
            (->package
              "e"
              5
              "e_loc5"
              nil)
            repo-info
            {"a" [package-a]
             "b" [package-b]
             "c" [package-c]
             "d" [package-d4 package-d3]
             "e" [package-e6 package-e5]}
            query
            (map-query repo-info)]
        (is (= [:successful
                #{package-a
                  package-b
                  package-c
                  package-d3
                  package-e5}]
               (resolve-dependencies
                 [
                  [(present "a")]
                  ]
                 query)
               ))))
    (testing (str "The puzzle")
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
               [(present "e" #(= (:version %) 4))]
               ]
              )
            package-d2
            (->package
              "d"
              2
              "d_loc2"
              [
               [(present "e" #(= (:version %) 3))]
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
        (is (= [:successful #{package-a
                              package-b
                              package-c
                              package-d2
                              package-e3}]
               (resolve-dependencies
                 [
                  [(present "a")]
                  ]
                 query)))))
    (testing (str "Double diamond")
      (let [package-a
            (->package
              "a"
              1
              "a_loc1"
              [
               [(present "b")]
               [(present "d" #(>= (:version %) 1))]
               ]
              )
            package-b
            (->package
              "b"
              1
              "b_loc1"
              [
               [(present "c")]
               [(present "d" #(< (:version %) 4))]
               ]
              )
            package-c
            (->package
              "c"
              1
              "c_loc1"
              [
               [(present "d" #(= (:version %) 2))]
               ]
              )
            package-d4
            (->package
              "d"
              4
              "d_loc4"
              nil)
            package-d3
            (->package
              "d"
              3
              "d_loc3"
              nil)
            package-d2
            (->package
              "d"
              2
              "d_loc2"
              nil)
            repo-info
            {"a" [package-a]
             "b" [package-b]
             "c" [package-c]
             "d" [package-d4 package-d3 package-d2]}
            query (map-query repo-info)]

        (is (= [:successful #{package-d2 package-c package-b package-a}]
               (resolve-dependencies
                 [
                  [(present "a")]
                  ]
                 query))))))

(deftest
  ^:resolve-basic hoisting
  (let [package-a
        (->package
          "a"
          1
          "a_loc1"
          [
           [(present "b") (present "c")]
           ]
          )
        package-b
        (->package
          "b"
          1
          "b_loc1"
          [
           [(present "c")]
           [(present "d" #(< (:version %) 4))]
           ]
          )
        package-c
        (->package
          "c"
          1
          "c_loc1"
          nil)
        package-d
        (->package
          "d"
          1
          "d_loc1"
          [
           [(present "b") (absent "e")]
           ]
          )
        repo-info
        {"a" [package-a]
         "b" [package-b]
         "d" [package-d]}
        query (map-query repo-info)
        aclause
        [(present "a")]]
    (testing
      "Prefer what's installed"
      (is
        (=
          [:successful
           #{package-a}]
          (resolve-dependencies
            [[(present "a")]]
            query
            :present-packages {"c" package-c}))))
    (testing
      "Prefer conflicts over installs"
      (is
        (=
          [:successful
           #{package-d}]
          (resolve-dependencies
            [[(present "d")]]
            query
            :present-packages {"c" package-c}
            :conflicts {"e" [nil]}))))))

(deftest ^:resolve-data tutorial-test
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
           :location "http://example.com/repo/b-2.4.0.zip"
           :requirements [[{:status :present :id "c" :spec [{:relation :greater-equal :version "3.5"}]}]]
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
           :requirements [[{:status :present :id "e" :spec [{:relation :greater-equal :version "1.8"}]}]]
           }
          ]
         "d"
         [
          {
           :id "d"
           :version "0.8.0"
           :location "http://example.com/repo/d-0.8.0.zip"
           :requirements [[{:status :present :id "e"
                            :spec [
                                   {:relation :greater-equal :version "1.1"}
                                   {:relation :less-than :version "2.0"}]}]]
           }
          ]
         "e"
         [
          {
           :id "e"
           :version "1.8.0"
           :location "http://exmaple.com/repo/e-1.8.0.zip"
           :requirements []
           }
          {
           :id "e"
           :version "2.1.0"
           :location "http://exmaple.com/repo/e-2.1.0.zip"
           :requirements []
           }
          {
           :id "e"
           :version "2.4.0"
           :location "http://exmaple.com/repo/e-2.4.0.zip"
           ; A lack of requirements should work fine here,
           ; as this will yeild nil and work as an empty list.

           }
          ]
         }
        query
        (map-query repo-info)]
    (testing "A test of the tutorial."
      (is (=
           (set [
            "http://example.com/repo/b-2.4.0.zip"
            "http://example.com/repo/c-3.5.0.zip"
            "http://example.com/repo/d-0.8.0.zip"
            "http://exmaple.com/repo/e-1.8.0.zip"
            ])
           (set (map :location
                (get (resolve-dependencies
                 [[{:status :present :id "b" :spec [{:relation :greater-than :version "2.0"}]}]]
                 query
                 :compare older?) 1 :unsuccessful)))))))

(deftest ^:resolve-data-spec data-spec-cases
  (let [b1
        {
         :id "b"
         :version "1.0"
         :location "http://example.com/repo/b-1.0.zip"
         }
        b23
        {
         :id "b"
         :version "2.3"
         :location "http://example.com/repo/b-2.3.zip"
         }
        b20
        {
         :id "b"
         :version "2.0"
         :location "http://example.com/repo/b-2.0.zip"
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
                             :spec [{:relation :greater-than :version "2.0"}]}]]
                          query-asc
                          :compare older?))))
      (testing "greater-equal, less-than data spec case"
        (is (= [:successful #{b20}]
               (resolve-dependencies
                [[{:status :present
                   :id "b"
                   :spec [{:relation :greater-equal :version "2.0"}
                          {:relation :less-than :version "2.3"}]}]]
                query-mixed
                :compare older?))))
      (testing "less-equal data spec case"
        (is (= [:successful #{b20}]
               (resolve-dependencies
                [[{:status :present
                   :id "b"
                   :spec [{:relation :less-equal :version "2.3"}
                          {:relation :greater-than :version "2.0"}]}]]
                query-asc
                :compare older?))))
      (testing "equal-to data spec case"
        (is (= [:successful #{b20}]
               (resolve-dependencies
                [[{:status :present
                   :id "b"
                   :spec [{:relation :equal-to :version "2.0"}]}]]
                query-mixed
                :compare older?))))))
