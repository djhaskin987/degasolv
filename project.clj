(defproject degasolv/degasolv "1.0.2-SNAPSHOT"
  :description "Dependency resolver with an eye toward building software."
  :url "http://github.com/djhaskin987/degasolv"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main degasolv.cli
  :dependencies [
                 [org.clojure/clojure "1.9.0-alpha14"]
                 [version-clj "0.1.2"]
                 [org.clojure/tools.cli "0.3.5"]
                 [me.raynes/fs "1.4.6"]
                 ]
  :test-selectors
  {
   :resolve-basic :resolve-basic
   :resolve-harden :resolve-harden
   :string-to-requirement :string-to-requirement
   :repo-aggregation :repo-aggregation
   }
  :profiles {
             :dev {:dependencies [
                                  [org.clojure/core.match "0.3.0-alpha4"]
                                  [version-clj "0.1.2"
                                   :exclusions [org.clojure/clojure]]
                                  ]}
             :uberjar {:aot :all}
             }
  :target-path "target/%s")
