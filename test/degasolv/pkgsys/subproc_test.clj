(ns degasolv.pkgsys.subproc-test
  (:require [clojure.test :refer :all]
            [degasolv.resolver :refer :all]
            [degasolv.pkgsys.subproc :refer :all]))

(deftest ^:unit-tests convert-input-test
  (testing "Empty cases"
    (is (empty? (convert-input {})))
    (is (= {"a" [] "b" []}
           (convert-input {"a" [] "b" []}))))
  (testing "Basic Case"
    (is (= {"a" [(->PackageInfo "a" "1.0.0" "yurt" nil)]}
           (convert-input {"a" [{:id "a" :version "1.0.0" :location "yurt"}]}))))
  (testing "Metadata Case"
    (let [metadata-pkg (assoc (->PackageInfo "a" "1.0.0" "yurt" nil) :foo "bar")]
      (is (= {"a" [metadata-pkg]}
             (convert-input {"a" [{:id "a"
                                   :version "1.0.0"
                                   :location "yurt"
                                   :foo "bar"}]})))))
  (testing "Requirements case"
    (is (= {"a" [(->PackageInfo
                  "a"
                  "1.0.0"
                  "yurt" [[(->Requirement
                            :present
                            "b"
                            [[(->VersionPredicate :greater-equal "2.0")
                              (->VersionPredicate :less-than "3.0")]])
                           (->Requirement :present "c" nil)]])]}
            (convert-input {"a" [{:id "a" :version "1.0.0" :location "yurt" :requirements ["b>=2.0,<3.0|c"]}]})))))
