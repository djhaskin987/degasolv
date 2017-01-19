(ns dependable.cli
  (:require [dependable.util :refer :all]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.edn :as edn])
  (:gen-class))

(def cli-options
  [["-p" "--project-file FILE" "Project spec file"
    :default "./dependable.edn"
    :validate [
               #(.exists (io/as-file %))
               "Must be a regular file (which hopefully contains project info."]]])

(defn update-repo! [project-config options arguments]
  (println "goodbye"))

(def subcommand-cli
  {"update-repo"
   {:function update-repo!
    :cli [

          ]}})

(defn command-list [commands]
  (->> ["Commands are:"
        ""
        (string/join \newline (map #(str "  - " %) commands))
        ]
       (string/join \newline)))

(defn usage [options-summary & {:keys [sub-command]}]
  (let [display-command (if sub-command
                          sub-command
                          "<command>")]
    (->> [(str "Usage: dependable <options> "
               display-command
               " <"
               display-command
               "-options>")
          ""
          "Options are:"
          ""
          options-summary
          ]
         (string/join \newline))))

(defn error-msg [errors & {:keys [sub-command]}]
  (str "The following errors occurred while parsing commandline options"
       (if sub-command
         (str " for subcommand `" sub-command "`")
         "")
       ":\n\n"
       (string/join \newline (map #(str "  - " errors)))))

(defn exit [status msg]
  (.println *err* msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args (concat
                          cli-options
                          [["-h" "--help"]])
                    :in-order true)]
    (cond
      (:help options) (exit 0 (str (usage summary)
                                   "\n\n"
                                   (command-list (keys subcommand-cli))))
      errors (exit 1 (usage summary
                            "\n"
                            (command-list (keys subcommand-cli)))))
    (let [global-options options
          subcommand (first arguments)
          subcmd-cli (get subcommand-cli subcommand)]
      (when (nil? subcmd-cli)
        (exit 1 (error-msg (str "Unknown command: " subcommand))))
      (let [{:keys [options arguments errors summary]}
            (parse-opts args (concat
                              (:cli subcmd-cli)
                              [["-h" "--help"]]))]
        (cond
          (:help options) (exit 0 (usage summary :sub-command subcommand))
          errors (exit 1 (usage summary :sub-command subcommand)))
        ((:function subcmd-cli)
         (edn/read-string (slurp (:project-file global-options)))
         options arguments)))))
