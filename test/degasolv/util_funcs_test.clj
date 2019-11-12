(ns degasolv.util-funcs-test
  (:require [clojure.test :refer :all]
            [degasolv.util :refer :all]
            [clj-wiremock.core :refer :all]))

(deftest ^:integration-tests
         slurp-basic-auth
         (let [wiremock-server (server)]
           (start wiremock-server)
           (stub
             {:request
              {
               :method "GET"
               :url "/basic-auth"
               :headers {
                         :Authorization "Basic YWJjOjEyMw=="
                         }
               }
              :response
              {
               :status 200
               :body "boy oh boy"
               :headers {
                         :Content-Type "text/plain"
                         }
               }
              })
           (stub
             {:request
              {
               :method "GET"
               :url "/query-string"
               :queryParameters {
                                 :search "in"
                                 }
               }
              :response {
                         :headers {
                                   :Content-Type "text/plain"
                                   }
                         :status 200
                         :body "beef"
                         }

              })
           (testing "What does query string do"
                    (is (= "beef" (default-slurp "http://localhost:8080/query-string?in=tact"))))
           (testing "Basic authentication"
                    (is (= "boy oh boy" (default-slurp "http://abc:123@localhost:8080/basic-auth")))
                    )
           (stop wiremock-server)))
