(ns degasolv.cli
  (:require [degasolv.util :refer :all]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [clojure.pprint :as pprint]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [me.raynes.fs :as fs]
            [clj-semver.core :refer [cmp]])
  (:gen-class))

(defmacro dbg [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
x#))

(def cli-options
  [["-c" "--config-file FILE" "config file"
    :default (fs/file (fs/expand-home "~/.config/degasolv/config.edn"))
    :validate [#(and (fs/exists? %)
                     (fs/file? %))
               "Must be a regular file (which hopefully contains config info."]]])

(defn generate-repo-index!
  [config options arguments]
  (let [{:keys [add-to
                search-directory
                output-file]} options
        initial-repository
        (if add-to
          (edn/read-string
           (slurp add-to))
          {})]
    (with-open
      [ow (io/writer output-file :encoding "UTF-8")]
      (pprint/pprint
       (into {}
             (dbg (map
                   (fn [x]
                     [(first x)
                      (into []
                            (sort #(- (cmp (:version %1)
                                           (:version %2)))
                                  (second x)))])
                   (reduce
                    (fn merg [c v]
                      (update-in c [(:id v)] conj v))
                    initial-repository
                    (map
                     #(edn/read-string (slurp %))
                     (filter #(and (fs/file? %)
                                   (= ".dscard" (dbg (fs/extension %))))
                             (file-seq (fs/file search-directory))))))))

       ow))))

(def subcommand-cli
  {"generate-repo-index"
   {:description "Generate repository index based on degasolv package cards"
    :function generate-repo-index!
    :cli [["-a" "--add-to REPO_LOC"
           "Add to package information alread to be found at repo index REPO_LOC"]
          ["-o" "--output-file FILE"
           "The file to which to output the information."
          :default "index.dsrepo"]
          ["-d" "--search-directory DIR" "Directory to search for degasolv cards"
           :default "."
           :validate [#(and
                        (fs/directory? %)
                        (fs/exists? %))
                      "Must be a directory which exists on the file system."]]]}})

(defn command-list [commands]
  (->> ["Commands are:"
        ""
        (string/join \newline (map #(str "  - " %) commands))
        ""
        "Simply run `degasolv <command> -h` for help information."
        ""
        ]
       (string/join \newline)))

(defn usage [options-summary & {:keys [sub-command]}]
  (let [display-command (if sub-command
                          sub-command
                          "<command>")]
    (->> [(str "Usage: degasolv <options> "
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
                                   (command-list (keys subcommand-cli))
                                   "\n\n"
                                   ))
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
         (edn/read-string (slurp (:config-file global-options)))
         options arguments)))))
