(ns degasolv.cli-funcs-test
  (:require [clojure.test :refer :all]
            [degasolv.cli :refer :all]))

(deftest ^:unit-tests expand-option-packs-test
         (testing "Empty option packs test"
                  (is (empty? (expand-option-packs {}))))
         (testing "Disparate option packs"
                  (is (empty?
                        (expand-option-packs
                          {:option-packs ["a"]}))))
         (testing "Normal option packs expansion"
                  (is (=
                        {
                         :conflict-strat "prioritized"
                         :resolve-strat "fast"
                         :alternatives true
                         }
                        (expand-option-packs
                          {
                           :option-packs ["firstfound-version-mode"]
                           :alternatives true
                           }))))
         (testing "Option packs override each other"
                  (is (=
                        {
                         :conflict-strat "prioritized"
                         :resolve-strat "fast"
                         :alternatives false
                         :error-format false
                         :list-strat "as-set"
                         }
                        (expand-option-packs
                          {:option-packs ["v1"
                                        "multi-version-mode"
                                        "firstfound-version-mode"]})))))

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
                  (let [result (get-env-vars
                                 {"DEGASOLV_A" "it takes all kinds"
                                  "DEGASOLV_ALTERNATIVES" "true"
                                  "DEGASOLV_ERROR_FORMAT" "false"
                                  "DEGASOLV_CONFLICT_STRAT" "inclusive"
                                  "DEGASOLV_REQUIREMENTS" "a>=1,!=2;<=0^b|c"
                                  "DEGASOLV_REPOSITORIES" "https://a^https://b"
                                  "DEGASOLV_CONFIG_FILES" "a.edn^b.edn"
                                  "DEGASOLV_JSON_CONFIG_FILES" "a.json^b.json"
                                  "DEGASOLV_META" "a=1^b=2^c=3" })]
                    (is (=
                          {
                           :a "it takes all kinds"
                           :alternatives true
                           :error-format false
                           :conflict-strat "inclusive"
                           :requirements
                           ["a>=1,!=2;<=0"
                            "b|c"]
                           :repositories ["https://a", "https://b"]
                           :meta {
                                  "a" "1"
                                  "b" "2"
                                  "c" "3"
                                  }
                           }
                          (dissoc result :config-files)))
                    (is (= (count (:config-files result)) 4)))))
