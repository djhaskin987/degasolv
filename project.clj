(defproject degasolv/degasolv "1.6.0"
  :description "Dependency tracker with an eye toward building and shipping software."
  :url "http://github.com/djhaskin987/degasolv"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main degasolv.cli
  :dependencies [
                 [org.clojure/clojure "1.9.0-alpha14"]
                 [serovers "1.1.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [me.raynes/fs "1.4.6"]
                 [com.velisco/tagged "0.5.0"]
                 [tupelo "0.9.31"]
                 ]
  :plugins [[lein-print "0.1.0"]]
  :test-selectors
  {
   :resolve-basic :resolve-basic
   :resolve-harden :resolve-harden
   :string-to-requirement :string-to-requirement
   :repo-aggregation :repo-aggregation
   :resolve-conflict-strat :resolve-conflict-strat
   :resolve-disable-alternatives :resolve-disable-alternatives
   }
  :profiles {
             :dev {:dependencies [
                                  [tupelo "0.9.31"]
                                  [serovers "1.1.0"
                                   :exclusions [org.clojure/clojure]]
                                  ]}
             :uberjar {:aot [
                             degasolv.pkgsys.core
                             degasolv.pkgsys.apt
                             degasolv.util
                             degasolv.resolver
                             degasolv.cli
                             ]}
             }
  :target-path "target/%s")
