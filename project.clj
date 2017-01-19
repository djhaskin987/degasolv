(defproject org.clojars.djhaskin987/dependable "1.0.2-SNAPSHOT"
  :description "Dependency resolution for the impatient."
  :url "http://github.com/djhaskin987/dependable"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main dependable.cli
  :dependencies [
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [org.clojure/tools.cli "0.3.5"]
                 ]
  :test-selectors
  {
   :resolve-basic :resolve-basic
   :resolve-harden :resolve-harden
   }
  :profiles {
             :dev {:dependencies [
                                  [org.clojure/core.match "0.3.0-alpha4"]
                                  [grimradical/clj-semver "0.3.0-20130920.191002-3" :exclusions [org.clojure/clojure]]
                                  ]}
             :uberjar {:aot :all}
             }
  :target-path "target/%s")
