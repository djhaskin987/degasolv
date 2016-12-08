(ns dependable.main
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(defn process-args
  "args beginning with '--' are added to the options thing, with a value of true unless
   an equals sign is present. Args without that will be put in the options return value."
  [args]
  [nil nil])

(defn -main
    "I don't do a whole lot ... yet."
  [& args]
  
  (println "Hello, World!"))

(defn read-config-file
  "Reads the dependable configuration file."
  []
  nil)

(defn determine-project-root
  "Figures out the project root, relative to the given directory."
  [dir]
  nil)

(defn install-command
  "Runs the install command."
  [parameters options]
  nil)
