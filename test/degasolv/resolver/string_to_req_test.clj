(ns degasolv.resolver.string-to-req-test
  (:require [clojure.test :refer :all]
            [degasolv.resolver :refer :all]))

(deftest ^:unit-tests test-string-to-requirement-basic-cases
  (testing "Basic case"
    (is (= [(present "a")]
           (string-to-requirement "a"))))
  (testing "Empty case"
    (is (= []
           (string-to-requirement ""))))
  (testing "Comparative cases"
    (is (= [(present "a"
                     [[(->VersionPredicate :less-than
                                           "1.0.0")]])]
           (string-to-requirement "a<1.0.0")))
    (is (= [(present "a"
                     [[(->VersionPredicate :less-equal
                                           "whatever")]])]
           (string-to-requirement "a<=whatever")))
    (is (= [(present "a"
                     [[(->VersionPredicate :not-equal
                                           "notvalidated")]])]
           (string-to-requirement "a!=notvalidated")))
    (is (= [(absent "z"
                    [[(->VersionPredicate :not-equal
                                          "0000")]])]
           (string-to-requirement
            "!z!=0000")))
    (is (= [(absent "z"
                    [[(->VersionPredicate :equal-to
                                          "alakazam")]])]
           (string-to-requirement
            "!z==alakazam")))
    (is (= [(absent
             "z"
             [[(->VersionPredicate :greater-equal
                                   "barbar")]])]
           (string-to-requirement "!z>=barbar")))
    (is (= [(present
             "x"
             [[(->VersionPredicate :greater-than
                                   "2.3.3")]])]
           (string-to-requirement "x>2.3.3"))))
  (testing "Matches cases"
    (is (= [(present
             "a"
             [[(->VersionPredicate :matches
                                   "f[ea]{2}ture")]])]
           (string-to-requirement "a<>f[ea]{2}ture"))))
  (testing "Matching prints"
    (is (= "a<>f[ea]{2}ture"
           (str
            (present
             "a"
             [[(->VersionPredicate :matches
                                   "f[ea]{2}ture")]])))))
  (testing "Range cases"
    (is (= [(present
             "a"
             [[(->VersionPredicate :in-range
                                   "3")]])]
           (string-to-requirement "a=>3"))))
  (testing "Range prints"
    (is (= "a=>3"
           (str
            (present
             "a"
             [[(->VersionPredicate :in-range
                                   "3")]]))))))

(deftest ^:unit-tests test-string-to-requirement-illustrations
  (testing "Illustrative example"
    (is (= [(present "a"
                     [[(->VersionPredicate :greater-equal "3.0.0")
                       (->VersionPredicate :less-than "4.0.0")]
                      [(->VersionPredicate :greater-equal "2.0.0")
                       (->VersionPredicate :less-than "2.5.1")]])
            (present "b"
                     [[(->VersionPredicate :greater-equal
                                           "1.0.0")
                       (->VersionPredicate :not-equal
                                           "1.5.0")]])]
           (string-to-requirement
            "a>=3.0.0,<4.0.0;>=2.0.0,<2.5.1|b>=1.0.0,!=1.5.0"))))
  (testing "Managed dependencies"
    (is (= [(absent "a")
            (present "a"
                     [[(->VersionPredicate :greater-than "1.0.0")
                       (->VersionPredicate :less-equal "4.0.0")]
                      [(->VersionPredicate :greater-equal "6.0.0")
                       (->VersionPredicate :less-than "7.0.0")]])]
           (string-to-requirement
            "!a|a>1.0.0,<=4.0.0;>=6.0.0,<7.0.0")))))
