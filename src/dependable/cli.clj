(ns dependable.cli
  (:require [dependable.util :refer :all]
            [clojure.java.io :as io])
  (:gen-class))

(def cli-options
  ;; An option with a required argument
  [["-c" "--config-file FILE" "Configuration file"
    :default "./dependable.edn"
    :validate [
               #(.exists (io/as-file))
               "Must be a regular file (which hopefully contains project info."]]
   ;; A boolean option defaulting to nil
   ["-h" "--help"]])

(defn -main [& args]
  (parse-opts args cli-options))

(:require [clojure.tools.cli :refer [parse-opts]])



(defn -main [& args]
  (println "I am a robot."))
