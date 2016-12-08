(defproject dependable "0.2.1-SNAPSHOT"
  :description "Dependency resolution for the impatient."
  :url "http://github.com/djhaskin987/dependable"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/tools.cli "0.3.5"]
                 ]
  :main ^:skip-aot dependable.main
  :test-selectors
  {
   :default :resolve-basic
   :resolve-basic :resolve-basic
   :resolve-harden :resolve-harden
   }
  :profiles {
             :test {:dependencies [[org.apache.commons/commons-io "1.3.2"]]}
             :uberjar {:aot :all}
             }
  :target-path "target/%s")
                                        ;:test-selectors
                                        ;{
                                        ; :default :resolve-basic
                                        ; :resolve-basic :resolve-basic
                                        ; :resolve-harden :resolve-harden
                                        ; }
