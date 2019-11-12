(ns degasolv.util.core-test
  (:require [clojure.test :refer :all]
            [degasolv.util :refer :all]
            [clj-wiremock.core :refer :all]))

(deftest ^:integration-tests
         slurp-auth
         (let [wiremock-server (server)]
           (start wiremock-server)
           (stub
             {:request
              {
               :method "GET"
               :url "/unauthenticated"
               }
              :response
              {
               :status 200
               :body "bay"
               :headers {
                         :Content-Type "text/plain"
                         }
               }})
           (stub
             {:request
              {
               :method "GET"
               :url "/bearer-auth"
               :headers {
                         :Authorization { :equalTo "Bearer deadbeef" }
                         }
               }
              :response
              {
               :status 200
               :body "ay"
               :headers {
                         :Content-Type "text/plain"
                         }
               }})
           (stub
             {:request
              {
               :method "GET"
               :url "/basic-auth"
               :headers {
                         :Authorization { :equalTo "Basic YWJjOjEyMw==" }
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
           (testing "Basic authentication"
                    (is (= "bay" (default-slurp "http://localhost:8080/unauthenticated")))
                    (is (= "boy oh boy" (default-slurp "http://abc:123@localhost:8080/basic-auth")))
                    (is (= "ay" (default-slurp "http://deadbeef@localhost:8080/bearer-auth")))
           (stop wiremock-server))))

(deftest ^:unit-tests assoc-conj-basic
  (testing "Add to a blank map"
    (is (.equals {:a [1]}
           (assoc-conj {} :a 1))))
  (testing "Add to a map with an empty list"
    (is (.equals {:a [1]}
           (assoc-conj {:a []} :a 1))))
  (testing "Add to a map with an existing list"
    (is (.equals {:a [1 2]}
           (assoc-conj {:a [1]} :a 2)))))
