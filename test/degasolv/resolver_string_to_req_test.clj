(ns degasolv.resolver-string-to-req-test
  (:require [clojure.test :refer :all]
            [degasolv.resolver :refer :all]))

(deftest ^:string-to-requirement test-string-to-requirement-basic-cases
 (testing "Basic case"
  (is (= [{:status :present
           :id "a"}]
       (string-to-requirement "a"))))
 (testing "Empty case"
  (is (= []
       (string-to-requirement ""))))
 (testing "Denial case"
  (is (= [{:status :absent
           :id "a"}]
       (string-to-requirement "!a"))))
 (testing "Comparative cases"
  (is (= [{:status :present
           :id "a"
           :spec [[{:relation :less-than
                    :version "1.0.0"}]]}]
       (string-to-requirement "a<1.0.0")))
  (is (= [{:status :present
           :id "a"
           :spec [[{:relation :less-equal
                    :version "whatever"}]]}]
       (string-to-requirement "a<=whatever")))
  (is (= [{:status :present
           :id "a"
           :spec [[{:relation :not-equal
                    :version "notvalidated"}]]}]
       (string-to-requirement "a!=notvalidated")))
  (is (= [{:status :absent
           :id "z"
           :spec [[{:relation :not-equal
                    :version "0000"}]]}]
       (string-to-requirement
        "!z!=0000")))
  (is (= [{:status :absent
           :id "z"
           :spec [[{:relation :equal-to
                    :version "alakazam"}]]}]
       (string-to-requirement
        "!z==alakazam")))
  (is (= [{:status :absent
           :id "z"
           :spec [[{:relation :greater-equal
                    :version "barbar"}]]}]
       "!z>=barbar"))
  (is (= [{:status :present
           :id "x"
           :spec [[{:relation :greater-than
                    :version "2.3.3"}]]}]
       "x>2.3.3"))))

(deftest ^:string-to-requirement test-string-to-requirement-illustrations
  (testing "Illustrative example"
    (is (= [{:status :present
             :id "a"
             :spec
             [[{:relation :greater-equal
                :version "3.0.0"}
               {:relation :less-than
                :version "4.0.0"}]
              [{:relation :greater-equal
                :version "2.0.0"}
               {:relation :less-than
                :version "2.5.1"}]]}
            {:status :present
             :id "b"
             :spec
             [[{:relation :greater-equal
                :version "1.0.0"}
               {:relation :not-equal
                :version "1.5.0"}]]}]
           (string-to-requirement
            "a>=3.0.0,<4.0.0;>=2.0.0,<2.5.1|b>=1.0.0,!=1.5.0"))))
  (testing "Managed dependencies"
    (is (= [{:status :absent
             :id "a"}
            {:status :present
             :id "a"
             :spec
             [[{:relation :greater-than
                :version "1.0.0"}
               {:relation :less-equal
                :version "4.0.0"}]
              [{:relation :greater-equal
                :version "6.0.0"}
               {:relation :less-than
                :version "7.0.0"}]]}]
           (string-to-requirement
            "!a|a>1.0.0,<=4.0.0;>=6.0.0,<7.0.0")))))
