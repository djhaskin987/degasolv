(defproject dependable "0.1.0-SNAPSHOT"
            :description "Dependency resolution for the impatient."
            :url "http://example.com/FIXME"
            :license {:name "Eclipse Public License"
                      :url "http://www.eclipse.org/legal/epl-v10.html"}
            :dependencies [[org.clojure/clojure "1.7.0"]]
            :main ^:skip-aot dependable.core
            :target-path "target/%s"
            :test-selectors {:underpinnings :underpinnings}
            :profiles {:uberjar {:aot :all}

                       :user {:plugins [[lein-cloverage "1.0.6"]]}}
            )
