(ns dependable.resolve-test
  (:require [clojure.test :refer :all]
            [dependable.core :refer :all])
  (:import [dependable.core
            package
            requirement]))

(defmacro debug [form]
  `(let [x# ~form]
     (println (str "Debug: "
                   (quote ~form)
                   " is " x#))
     x#))

(defn map-query [m]
  (fn [nm]
    (let [result (find m nm)]
      (if (nil? result)
        []
        (let [[k v] result]
          v)))))


(let  [package-a30
       #dependable.core.package
       {
        :id "a"
        :version 30
        :location "a_loc30"
        :requirements
        [
         #dependable.core.requirement
         {
          :status :present
          :id "c"
          }
         ]
        }
       package-a20
       #dependable.core.package
       {
        :id "a"
        :version 20
        }
       package-c10
       #dependable.core.package
       {
        :id "c"
        :version 10
        :location "c_loc10"
        }
       package-d22
       #dependable.core.package
       {
        :id "d"
        :version 22
        :location "d_loc22"
        }
       package-e18
       #dependable.core.package
       {:id "e"
        :version 18
        :location "e_loc18"
        :requirements
        [
         #dependable.core.requirement
         {
          :id "d"
          }
         ]
        }
       repo-info
       {"a"
        [package-a30
         package-a20]
        "c"
        [package-c10]
        "d"
        [package-d22]}
       query (map-query repo-info)]
  (deftest ^:resolve-basic retrieval
           (testing "Asking for a present package succeeds."
                    (is (= (resolve-dependencies
                             [[
                               #dependable.core.requirement
                               {
                                :id "a"
                                }]]
                             query)
                           [:successful #{package-a30}])))
           (is (= (resolve-dependencies
                    [[#dependable.core.requirement
                      {
                       :id "c"
                       }]]
                    query)
                  [:successful #{package-c10}]))
           (testing "Asking for a nonexistent package fails."
                    (let [b-clause [
                                    #dependable.core.requirement
                                    {
                                     :id "b"
                                     }]]
                      (is (= (resolve-dependencies
                               [b-clause]
                               query)
                             [:unsuccessful b-clause]))))
           (testing (str "Asking for a package present within the repo but "
                         "at no suitable version fails.")
                    (let [a-clause
                          [
                           #dependable.core.requirement
                           {
                            :id "a"
                            :spec #(>= (:version %1) 40)
                            }]]
                      (is (= (resolve-dependencies
                               [a-clause]
                               query)
                             [:unsuccessful a-clause]))))
           (testing (str "Asking for a package present within the repo but "
                         "with unsuccessful constraints.")
                    (let [a-clause
                          [
                           {
                            :id "a"
                            :spec #(false)
                            }]]
                      (is (= (resolve-dependencies
                               [a-clause]
                               query)
                             [:unsuccessful a-clause]))))
           (testing (str "Asking for a package present and having a "
                         "version that fits")
                    (is (= (resolve-dependencies
                             [{:id "a"
                               :version-spec #(and (>= %1 15) (<= %1 25))}]
                             query)
                           [:successful #{package-a20}]))
                    (is (= (resolve-dependencies
                             [{:id "a"
                               :version-spec #(>= %1 25)}]
                             query)
                           [:successful #{package-a30}]))))
(deftest present-packages
         (testing "Asking to install a package twice."
                  (is (= (resolve-dependencies
                           [{:id "a"}
                            {:id "a"}]
                           query)
                         [:successful #{package-a30}])))
         (testing (str "Asking to install a package that I have given as "
                       "already installed.")
                  (is (= (resolve-dependencies [{:id "c"}]
                                               query
                                               :present-packages {"c" 18})
                         [:successful #{}])))
         (testing (str "Asking to install a package that I have given as already "
                       "installed, even though the package isn't available.")
                  (is (= (resolve-dependencies
                           [{:id "b"}]
                           query
                           :present-packages {"b" 30})
                         [:successful #{}])))
         (testing (str "Asking to install a package that is already "
                       "installed, but the installed version doesn't "
                       "suit, even though there is a suitable version "
                       "available.")
                  (is (= (resolve-dependencies
                           [{:id "a"
                             :version-spec #(>= % 25)}]
                           query
                           :present-packages {"a" 15})
                         [:unsuccessful ["a"]])))
         (testing (str "Asking to install a package that is already "
                       "installed, and the installed version suits.")
                  (is (= (resolve-dependencies
                           [{:id "a"
                             :version-spec #(>= % 25)}]
                           query
                           :present-packages {"a" 30})
                         [:successful #{}]))))
(deftest conflicts
         (testing (str "Find a package which conflicts with another "
                       "package also to be installed.")
                  (is (= (resolve-dependencies
                           [{:id "d"}
                            {:id "e"}]
                           query)
                         [:unsuccessful ["e"]])))
         (testing (str "Find a package which conflicts with a package "
                       "marked a priori as conflicting.")
                  (is (= (resolve-dependencies
                           [{:id "a"}
                            {:id "d"}]
                           query
                           :conflicts {"d" nil})
                         [:unsuccessful ["d"]])))
         (testing (str "Find a package which conflicts with a package "
                       "marked a priori as conflicting with something "
                       "already installed.")
                  (is (= (resolve-dependencies
                           [{:id "d"}]
                           query
                           :present-packages {"a" 11}
                           :conflicts {"d" nil})
                         [:unsuccessful ["d"]])))

         (testing (str "Find a package which conflicts with another "
                       "package but not at its current version")
                  (is (= (resolve-dependencies
                           [{:id "d"}]
                           query
                           :conflicts {"d" #(< % 22)})
                         [:successful #{package-d22}])))))


#_(deftest ^:resolve-basic requires
           (let [package-a
                 {:id "a"
                  :version 30
                  :location "a_loc30"
                  :requires [{:id "b"}]}
                 package-b
                 {:id "b"
                  :version 20
                  :location "b_loc20"}
                 repo-info
                 {"a" [package-a]
                  "b" [package-b]}
                 query (map-query repo-info)]
             (testing (str "One package should require another and both "
                           "should be found.")
                      (is (= (resolve-dependencies
                               [{:id "a"}]
                               query)
                             [:successful #{package-a package-b}])))
             (testing (str "One package should be found when it requires "
                           "another, but it's already installed.")
                      (is (= (resolve-dependencies
                               [{:id "a"}]
                               query
                               :present-packages {"b" 20})
                             [:successful #{package-a}]))))
           (let [package-a
                 {:id "a"
                  :version 10
                  :location "a_loc30"}
                 package-b
                 {:id "b"
                  :version 20
                  :location "b_loc20"
                  :requires [{:id "c"}]}
                 package-c
                 {:id "c"
                  :version 10
                  :conflicts {"a" nil}}

                 repo-info
                 {"a" [package-a]
                  "b" [package-b]
                  "c" [package-c]}
                 query (map-query repo-info)]
             (testing (str "A package having dependencies which conflicts with "
                           "other packages downloaded should be rejected.")
                      (is (= (resolve-dependencies
                               [{:id "a"}
                                {:id "b"}]
                               query)
                             [:unsuccessful ["b" "c"]]))))
           (let [package-a
                 {:id "a"
                  :version 10
                  :location "a_loc10"}
                 package-b
                 {:id "b"
                  :version 10
                  :location "b_loc10"
                  :requires [{:id "c"}]}
                 package-c
                 {:id "c"
                  :version 10
                  :local "c_loc10"}
                 repo-info
                 {"a" [package-a]
                  "b" [package-b]
                  "c" [package-c]}
                 query (map-query repo-info)]
             (testing (str "A package having dependencies rejected a priori "
                           "also gets rejected")
                      (is (= (resolve-dependencies
                               [{:id "a"}
                                {:id "b"}]
                               query
                               :conflicts {"c" nil})
                             [:unsuccessful ["b" "c"]])))))

#_(deftest ^:resolve-basic no-locking
           (testing (str "Find two packages, even when the preferred version "
                         "of one package conflicts with the other")
                    (let [package-a30
                          {:id "a"
                           :version 30
                           :location "a_loc30"
                           :conflicts {"c" nil}
                           }
                          package-a20
                          {:id "a"
                           :version 20
                           :location "a_loc20"}
                          package-c10
                          {:id "c"
                           :version 10
                           :location "c_loc10"}
                          repo-info
                          {"a" [package-a30 package-a20]
                           "c" [package-c10]}
                          query (map-query repo-info)]
                      (is (= (resolve-dependencies
                               [{:id "a"}
                                {:id "c"}]
                               query)
                             [:successful #{package-a20 package-c10}]))))
           (testing (str "Diamond problem")
                    (let [package-a
                          {:id "a"
                           :version 1
                           :location "a_loc1"
                           :requires [{:id "b"}
                                      {:id "c"}]}
                          package-b
                          {:id "b"
                           :version 1
                           :location "b_loc1"
                           :requires [{:id "d"
                                       :version-spec #(>= % 2)}]}
                          package-c
                          {:id "c"
                           :version 1
                           :location "c_loc1"
                           :requires [{:id "d"
                                       :version-spec #(< % 4)}]}
                          package-d3
                          {:id "d"
                           :version 3
                           :location "d_loc3"}
                          package-d4
                          {:id "d"
                           :version 4
                           :location "d_loc4"}
                          repo-info
                          {"a" [package-a]
                           "b" [package-b]
                           "c" [package-c]
                           "d" [package-d4 package-d3]}
                          query
                          (map-query repo-info)]
                      (is (= (resolve-dependencies
                               [{:id "a"}]
                               query)
                             [:successful
                              #{package-a
                                package-b
                                package-c
                                package-d3}]))))
           (testing (str "Inter-Locking Diamond problem")
                    (let [package-a
                          {:id "a"
                           :version 1
                           :location "a_loc1"
                           :requires [{:id "b"}
                                      {:id "c"}]}
                          package-b
                          {:id "b"
                           :version 1
                           :location "b_loc1"
                           :requires [{:id "d"
                                       :version-spec #(>= % 2)}
                                      {:id "e"
                                       :version-spec #(= % 5)}]}
                          package-c
                          {:id "c"
                           :version 1
                           :location "c_loc1"
                           :requires [{:id "e"
                                       :version-spec #(>= % 1)}
                                      {:id "d"
                                       :version-spec #(< % 4)}]}
                          package-d3
                          {:id "d"
                           :version 3
                           :location "d_loc3"}
                          package-d4
                          {:id "d"
                           :version 4
                           :location "d_loc4"}
                          package-e6
                          {:id "e"
                           :version 6
                           :location "e_loc6"}
                          package-e5
                          {:id "e"
                           :version 6
                           :location "e_loc5"}
                          repo-info
                          {"a" [package-a]
                           "b" [package-b]
                           "c" [package-c]
                           "d" [package-d4 package-d3]
                           "e" [package-e6 package-e5]}
                          query
                          (map-query repo-info)]
                      (is (= (resolve-dependencies
                               [{:id "a"}]
                               query)
                             [:successful
                              #{package-a
                                package-b
                                package-c
                                package-d3
                                package-e5}]))))
(testing (str "The puzzle")
         (let [package-a
               {:id "a"
                :version 1
                :location "a_loc1"
                :requires [{:id "b"}
                           {:id "c"}]}
               package-b
               {:id "b"
                :version 1
                :location "b_loc1"
                :requires [{:id "d"
                            :version-spec #(>= % 1)}]}
               package-c
               {:id "c"
                :version 1
                :location "c_loc1"
                :requires [{:id "d"
                            :version-spec #(< % 4)}]}
               package-d1
               {:id "d"
                :version 1
                :location "d_loc1"
                :requires [{:id "e"
                            :version-spec #(= % 4)}]}
               package-d2
               {:id "d"
                :version 2
                :location "d_loc2"
                :requires [{:id "e"
                            :version-spec #(= % 3)}]}
               package-e4
               {:id "e"
                :version 4
                :location "e_loc4"}
               package-e3
               {:id "e"
                :version 3
                :location "e_loc3"}
               repo-info
               {"a" [package-a]
                "b" [package-b]
                "c" [package-c]
                "d" [package-d2 package-d1]
                "e" [package-e4 package-e3]}
               query
               (map-query repo-info)]
           (is (= (resolve-dependencies
                    [{:id "a"}]
                    query)
                  [:successful #{package-a
                                 package-b
                                 package-c
                                 package-d1
                                 package-e4}]))))
(testing (str "Double diamond")
         (let [package-a
               {:id "a"
                :version 1
                :location "a_loc1"
                :requires [{:id "b"}
                           {:id "d"
                            :version-spec #(>= % 1)}]}
               package-b
               {:id "b"
                :version 1
                :location "b_loc1"
                :requires [{:id "d"
                            :version-spec #(< % 4)}]}
               package-c
               {:id "c"
                :version 1
                :location "c_loc1"
                :requires [{:id "d"
                            :version-spec #(= % 2)}]}
               package-d4
               {:id "d"
                :version 4
                :location "d_loc4"}
               package-d3
               {:id "d"
                :version 3
                :location "d_loc3"}
               package-d2
               {:id "d"
                :version 2
                :location "d_loc2"}
               repo-info
               {"a" [package-a]
                "b" [package-b]
                "c" [package-c]
                "d" [package-d4 package-d3 package-d2]}
               query (map-query repo-info)]
           (is (= (resolve-dependencies
                    [{:id "a"}]
                    query)
                  [:successful #{package-d2 package-c package-b package-a}])))))