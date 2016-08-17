(defproject dependable "0.2.0"
            :description "Dependency resolution for the impatient."
            :url "http://example.com/FIXME"
            :license {:name "Eclipse Public License"
                      :url "http://www.eclipse.org/legal/epl-v10.html"}
            :dependencies [[org.clojure/clojure "1.8.0"]
                           [org.clojure/core.match "0.3.0-alpha4"]]
            :main ^:skip-aot dependable.core
            :target-path "target/%s"
            :test-selectors
            {
             :default :resolve-basic
             :resolve-basic :resolve-basic
             :resolve-harden :resolve-harden
             }
            :profiles {:uberjar {:aot :all}})


