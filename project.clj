(defproject degasolv/degasolv "2.1.0"
            :description "Dependency tracker with an eye toward building and shipping software."
            :url "http://github.com/djhaskin987/degasolv"
            :license {:name "Eclipse Public License"
                      :url "http://www.eclipse.org/legal/epl-v10.html"}
            :main degasolv.cli
            :dependencies [
                           ;; ordered set for version suggestion
                           [org.flatland/ordered "1.5.7"]
                           ;; optional dependencies to clj-http
                           [cheshire "5.9.0"]
                           ;;[org.clojure/tools.reader "1.3.2"]
                           ;;[crouton "0.1.2"]
                           ;;[ring/ring-codec "1.1.2"]
                           ;;[
                           [com.velisco/tagged "0.5.0"]
                           [org.clojure/clojure "1.10.1"]
                           [org.clojure/tools.cli "0.3.5"]
                           [serovers "1.6.2"]
                           ]
            :plugins [[lein-licenses "0.2.2"]
                      [lein-print "0.1.0"]]

            :java-source-paths ["src/java"
                                "test/java"]
            :junit ["test/java"]
            ;;:source-paths ["src/degasolv"]

            :global-vars {*warn-on-reflection* true}

            :test-selectors
            {
             :unit-tests :unit-tests
             }
            :profiles {
                       :dev {
                             :dependencies [
                                            [pjstadig/humane-test-output "0.9.0"]
                                            ;; ordered set for version suggestion
                                            [org.flatland/ordered "1.5.7"]
                                            [org.clojure/core.match "0.3.0-alpha5"]
                                            [org.clojure/clojure "1.10.1"]
                                            [serovers "1.6.2"]
                                            [org.clojure/tools.cli "0.3.5"]
                                            [com.velisco/tagged "0.5.0"]
                                            ;; optional dependencies to clj-http
                                            [cheshire "5.9.0"]
                                            ]
                             :plugins [[test2junit "1.3.3"]]
                             :test2junit-output-dir "target/test-results"
                             :injections [(require 'pjstadig.humane-test-output)
                                          (pjstadig.humane-test-output/activate!)]
                             }
                       :uberjar {:aot [
                                       degasolv.pkgsys.core
                                       degasolv.pkgsys.apt
                                       degasolv.pkgsys.subproc
                                       degasolv.pkgsys.git
                                       degasolv.util
                                       degasolv.resolver
                                       degasolv.cli
                                       ]}
                       }
            :target-path "target/%s/"
            )
