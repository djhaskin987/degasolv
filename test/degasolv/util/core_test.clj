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
               :url "/raw-query-string"
               :queryParameters {
                                 :in { :equalTo "tact" }
                         }
               }
              :response
              {
               :status 200
               :body "raw query string"
               :headers {
                         :Content-Type "text/plain"
                         }
               }})
           (stub
             {
              :request
              {
               :method "GET"
               :url "/header-auth"
               :headers {
                         :X-Auth-Token { :equalTo "da7a=" }
                         }
               :queryParameters {
                                 :q { :equalTo "s" }
                                 }
               }
              :response
              {
               :status 200
               :body "header auth"
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
           (testing "different authentication techniques"
                    (is (= "raw query string"
                           (default-slurp "http://localhost:8080/raw-query-string?in=tact")))
                    (is (= "bay" (default-slurp "http://localhost:8080/unauthenticated")))
                    (is (= "boy oh boy" (default-slurp "http://abc:123@localhost:8080/basic-auth")))
                    (is (= "ay" (default-slurp "http://deadbeef@localhost:8080/bearer-auth")))
                    (is (= "header auth" (default-slurp "http://X-Auth-Token=da7a%3D@localhost:8080/header-auth?q=s"))))
           (stop wiremock-server)))
