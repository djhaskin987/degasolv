(defproject degasolv/degasolv "2.0.0-SNAPSHOT"
  :description "Dependency tracker with an eye toward building and shipping software."
  :url "http://github.com/djhaskin987/degasolv"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main degasolv.cli
  :dependencies [
                 [org.clojure/clojure "1.10.1"]
                 [serovers "1.6.2"]
                 [org.clojure/tools.cli "0.3.5"]
                 [com.velisco/tagged "0.5.0"]
                 [org.clojure/data.json "0.2.6"]
                 ]
  :plugins [[lein-licenses "0.2.2"]
            [lein-print "0.1.0"]]
;;  :source-paths ["src/degasolv"]

              :java-source-paths ["src/java" "test/java"]
              :junit ["test/java"]


  :global-vars {*warn-on-reflection* true}

  :test-selectors
  {
   :unit-tests :unit-tests
   }
  :profiles {
             :dev {
                   :dependencies [
                                  [org.clojure/core.match "0.3.0-alpha5"]
                                  [org.clojure/clojure "1.9.0-alpha14"]
                                  [serovers "1.6.2"]
                                  [org.clojure/tools.cli "0.3.5"]
                                  [com.velisco/tagged "0.5.0"]
                                  [org.clojure/data.json "0.2.6"]
                                  ]
                   :plugins [[test2junit "1.3.3"]]
                   :test2junit-output-dir "target/test-results"
                   }
             :uberjar {:aot [
                             degasolv.pkgsys.core
                             degasolv.pkgsys.apt
                             degasolv.pkgsys.subproc
                             degasolv.util
                             degasolv.resolver
                             degasolv.cli
                             ]}
             }
  :target-path "target/%s/"
  )
