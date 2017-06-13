(defproject degasolv/degasolv "1.2.0"
  :description "Dependency tracker with an eye toward building and shipping software."
  :url "http://github.com/djhaskin987/degasolv"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main degasolv.cli
  :dependencies [
                 [org.clojure/clojure "1.9.0-alpha14"]
                 [version-clj "0.1.2"]
                 [org.clojure/tools.cli "0.3.5"]
                 [me.raynes/fs "1.4.6"]
                 [com.velisco/tagged "0.5.0"]
                 ]
  :plugins [[lein-print "0.1.0"]]
  :test-selectors
  {
   :resolve-basic :resolve-basic
   :resolve-harden :resolve-harden
   :string-to-requirement :string-to-requirement
   :repo-aggregation :repo-aggregation
   :resolve-conflict-strat :resolve-conflict-strat
   }
  :profiles {
             :dev {:dependencies [
                                  [org.clojure/core.match "0.3.0-alpha4"]
                                  [version-clj "0.1.2"
                                   :exclusions [org.clojure/clojure]]
                                  ]}
             :uberjar {:aot [degasolv.cli degasolv.resolver]}
             }
  :target-path "target/%s")
