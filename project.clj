(defproject degasolv/degasolv "1.10.0"
  :description "Dependency tracker with an eye toward building and shipping software."
  :url "http://github.com/djhaskin987/degasolv"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main degasolv.cli
  :dependencies [
                 [org.clojure/clojure "1.9.0-alpha14"]
                 [serovers "1.6.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [me.raynes/fs "1.4.6"]
                 [com.velisco/tagged "0.5.0"]
                 [tupelo "0.9.31"]
                 [org.clojure/data.json "0.2.6"]
                 ]
  :plugins [[lein-print "0.1.0"]]
;;  :source-paths ["src/degasolv"]

              :java-source-paths ["src/java" "test/java"]
              :junit ["test/java"]




  :test-selectors
  {
   :unit-tests :unit-tests
   }
  :profiles {
             :dev {
                   :plugins [[test2junit "1.3.3"]]
                   :test2junit-output-dir "target/test-results"
                   }
             :uberjar {:aot [
                             degasolv.pkgsys.core
                             degasolv.pkgsys.apt
                             degasolv.util
                             degasolv.resolver
                             degasolv.cli
                             ]}
             }
  :target-path "target/%s/"
  )
