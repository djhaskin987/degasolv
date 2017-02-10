; -*- coding: utf-8 -*-
; vi: syntax=clojure fileencoding=utf8
(require
 '[boot.pod :as pod]
 '[clojure.java.io :as io])


(def uber-dependencies
 '[[org.clojure/clojure "1.9.0-alpha14"]
 [version-clj "0.1.2"]
 [org.clojure/tools.cli "0.3.5"]
 [me.raynes/fs "1.4.6"]
 [adzerk/boot-test "1.2.0"]
 [org.clojure/core.match "0.3.0-alpha4"]
 [com.velisco/tagged "0.5.0"]])

(def testing-dependencies
 (into uber-dependencies
  '[
  [adzerk/boot-test "1.2.0"]
  [org.clojure/core.match "0.3.0-alpha4"]
  ]))

(def testing-env {
 :project "degasolv"
 :version "1.0.2-SNAPSHOT"
 :resource-paths #{"src"}
 :repositories   '[["central" "https://repo1.maven.org/maven2/"]
 ["clojars" "http://clojars.org/repo"]
 ["Animalia nexus" "http://62.89.42.8:8082/nexus/content/groups/public"]]
 :dependencies testing-dependencies
 :source-paths #{"src" "test"}
 })

(def uber-env (into testing-env
               {:dependencies uber-dependencies
               :source-paths #{"src"}}))

(apply set-env! (apply concat testing-env))
(require '[adzerk.boot-test :refer :all])

(task-options!
 jar    {:main 'degasolv.cli}
 target {:dir #{"target"}}
 sift   {:include #{#"\.jar$"}}
 aot    {:namespace #{'degasolv.cli
 'degasolv.resolver}}
 pom    {:project 'degasolv/degasolv
 :version "1.0.2-SNAPSHOT"
 :url "http://github.com/djhaskin987/degasolv"
 :description "Dependency resolver with an eye toward building software."
 :license {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask testing
  "Profile setup for running tests."
  []
  (set-env! :source-paths #(conj % "test"))
  identity)

(deftask uberjar
 []
 (apply set-env! (apply concat uber-env))
 (comp (uber)
  (aot)
  (pom)
  (jar :file (format "%s-%s-standalone.jar" (get-env :project) (get-env :version)))
  (sift)
  (target)))
