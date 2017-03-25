(ns degasolv.pkgsys.apt-test
  (:require [clojure.test :refer :all]
            [degasolv.resolver :refer :all]
            [degasolv.pkgsys.apt :refer :all]))

(deftest ^:pkgsys-apt deb-to-degasolv-requirements-test
    (testing "Empty cases"
      (is (= nil
            (deb-to-degasolv-requirements nil)))
      (is (= nil
            (deb-to-degasolv-requirements ""))))
    (testing "Normal cases"
      (is (= [[(->Requirement
                :present
                "a"
                [[(->VersionPredicate
                    :greater-than
                    "5.0")]])]
              [(->Requirement
                :present
                "b"
                [[(->VersionPredicate
                    :greater-equal
                    "4.0")]])]]
             (deb-to-degasolv-requirements "a (>>5.0), b (>= 4.0)")))
        (is (= [[(->Requirement
                   :present
                   "a"
                   nil)
                 (->Requirement
                   :present
                   "b"
                   [[(->VersionPredicate
                       :less-than
                       "1.2.3")]])]
                [(->Requirement
                   :present
                   "c"
                   [[(->VersionPredicate
                       :equal-to
                       "1.0.0")]])]]
               (deb-to-degasolv-requirements
                "a|b (<< 1.2.3), c (= 1.0.0)")))))

(deftest ^:pkgsys-apt test-group-package-lines
    (testing "Empty cases"
      (is (empty?
            (group-pkg-lines '())))
      (is (=
            [[""]]
            (group-pkg-lines [""]))))
    (testing "Edge cases"
             (is (= [["Package: "]
                      ["foo"]]
                    (group-pkg-lines
                      ["Package: "
                       "foo"])))
             (is (= [["Package: foo"]
                     ["Package: bar" "Version: 1.0.0"]
                     ["Package: baz"]]
                    (group-pkg-lines
                      ["Package: foo"
                       "Package: bar"
                       "Version: 1.0.0"
                       "Package: baz"]))))
    (testing "Normal Cases"
             (is (= [["Package: foo"
                      "Version: 1.0.0"]
                     ["Package: bar"
                      "Version: 2.0.0"]
                     ["Package: baz"
                      "Version: 3.0.0"]]
                    (group-pkg-lines
                      ["Package: foo"
                       "Version: 1.0.0"
                       "Package: bar"
                       "Version: 2.0.0"
                       "Package: baz"
                       "Version: 3.0.0"])))))

; (defn lines-to-map
;   [lines]
;   (as-> lines it
;         (map
;           (fn [line]
;             (let [[_ k v] (re-matches #"^([^:]+): +(.*)$" line)]
;               [(keyword
;                 (string/lower-case k))
;               v]))
;           it)
;         (into {} it)))
