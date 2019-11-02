(ns degasolv.cli-funcs-test
  (:require [clojure.test :refer :all]
            [degasolv.cli :refer :all]))

(deftest ^:unit-tests env-vars-test
         (testing "Works okay with no vars"
                  (is (empty? (get-env-vars nil))))
         (testing "Returns no mismatching variables"
                  (is (empty? (get-env-vars {"DEGASOLV_" "Didn't have anything else after the underscore"
                                             "degasolv_a" "lowercase, doesn't count"
                                             "DEGASOLv_b" "mixed case prefix, doesn't count"
                                             "DEGASOLV_b" "mixed case suffix, doesn't count"
                                             "ALTERNATIVES" "lacks prefix"}))))
         (testing "Boolean arguments raise exception"
                  (is (thrown?
                        Exception
                        (get-env-vars {"DEGASOLV_ALTERNATIVES" "TRUE"})))
                  (is (thrown?
                        Exception
                        (get-env-vars {"DEGASOLV_ERROR_FORMAT" "fAlSe"})))
                  (is (thrown?
                        Exception
                        (get-env-vars {"DEGASOLV_ERROR_FORMAT" ""})))
                  (is (thrown?
                        Exception
                        (get-env-vars {"DEGASOLV_ALTERNATIVES"
                                       "literally any other value"}))))
         (testing "Filters out the right ones"
                  (is (=
                        {:a "it takes all kinds"
                         :alternatives true
                         :error-format false
                         :conflict-strat "inclusive"
                         :requirements
                         ["a>=1,!=2;<=0"
                          "b|c"]}
                        (get-env-vars
                          {"DEGASOLV_A" "it takes all kinds"
                           "DEGASOLV_ALTERNATIVES" "true"
                           "DEGASOLV_ERROR_FORMAT" "false"
                           "DEGASOLV_CONFLICT_STRAT" "inclusive"
                           "DEGASOLV_REQUIREMENTS" "a>=1,!=2;<=0^b|c" })))))
