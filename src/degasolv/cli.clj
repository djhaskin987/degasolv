(ns degasolv.cli
  (:require [degasolv.util :refer :all]
            [degasolv.resolver :as r :refer :all]
            [clojure.tools.cli :refer [parse-opts summarize]]
            [clojure.string :as string]
            [clojure.pprint :as pprint]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.spec :as s]
            [me.raynes.fs :as fs]
            [version-clj.core
             :refer [version-compare]
             :rename {version-compare cmp}
             ])

  (:gen-class))

; UTF-8 by default :)
(defn- default-slurp [loc]
  (clojure.core/slurp loc :encoding "UTF-8"))

(defn- default-spit [loc]
  (clojure.core/spit loc :encoding "UTF-8"))

#_(defmacro dbg [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
x#))

(defn- mysummary [sq]
  (with-out-str
    (pprint/pprint
     (map
      (fn [arg-parts]
        (select-keys arg-parts
                     [:short-opt
                      :long-opt
                      :required
                      :desc
                      :default-desc]))
      sq))))

(defn- translate-req-strings [card req]
  (if (string? req)
    (let [vetted-req (s/conform ::r/requirement-string req)]
      (if (= ::s/invalid vetted-req)
        (throw (ex-info
                 (str
                   "Invalid requirement string in card `"
                   card
                   "`")
                 (s/explain-data ::r/requirement-string
                                 req)))
        (string-to-requirement vetted-req)))
    req))

(defn- read-card!
  [card]
  (let [card-data (edn/read-string (default-slurp card))
        expanded-card-data
        (assoc card-data
               :requirements
               (map (partial translate-req-strings card)
                    (:requirements card-data)))
        vetted-card-data
        (s/conform ::r/package)]
    (if (= vetted-card-data
           ::s/invalid)
      (throw (ex-info (str
                        "Invalid data in card file `"
                        card
                        "`")
                      (s/explain-data ::r/package
                                      expanded-card-data)))
      expanded-card-data)))

(defn- generate-repo-index!
  [options arguments]
  (let [{:keys [add-to
                search-directory
                output-file]} options
        initial-repository
        (if add-to
          (edn/read-string
            (default-slurp add-to))
          {})]
    (with-open
      [ow (io/writer output-file :encoding "UTF-8")]
      (pprint/pprint
        (into {}
              (map
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
                    read-card!
                    (filter #(and (fs/file? %)
                                  (= ".dscard" (fs/extension %)))
                            (file-seq (fs/file search-directory)))))))
        ow))))

(defn- resolve-locations!
  [options arguments]
  (when (not (:repositories options))
    (throw (ex-info
            (str
             "No repositories specified\n"
             "(either through CLI or config file)"))))
  (let [{:keys [repositories
                resolve-strategy
                repo-merge-strategy]}
        options
        requirements
        (if (:project-file options)
          (let [project-info
                (edn/read-string
                  (default-slurp
                    (:project-file options)))]
            (when (not (:requirements project-info))
              (.println *err* "Warning: project file does not contain a `:requirements` key."))
            (:requirements project-info))
           (into [] (map
            #(string-to-requirement %)
            (rest arguments))))
        aggregator
        (if (= repo-merge-strategy
               "priority")
          priority-repo
          (fn [rs]
            (global-repo rs
                         :cmp #(- (cmp
                                   (:version %1)
                                   (:version %2))))))
        aggregate-repo
        (aggregator
         (map
          (fn slurp-url
            [url]
            (edn/read-string
             (default-slurp url)))
          repositories))
        result (resolve-dependencies
                requirements
                aggregate-repo
                :strategy (keyword resolve-strategy)
                :compare cmp)]
    (case
        (first result)
      :successful
      (let [[_ packages] result]
        (pprint/pprint
         (map
          (fn
            [pkg]
            [(:id pkg)
             (:location pkg)])
          packages)))
      :unsuccessful
      (let [[_ term] result]
        (throw (ex-info
                (str "\n\nCould not resolve dependencies.\n\n"
                     "The resolver choked on this term: "
                     (pprint/pprint term))
                {:term term}))))))

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
                      "Must be a directory which exists on the file system."]]]}
   "resolve-locations"
   {:description "Print the locations of the packages which will resolve all given dependencies."
    :function resolve-locations!
    :cli [["-r" "--repository REPO"
           "Specify a repository to use. May be used more than once."
           :id :repositories
           :assoc-fn
           (fn [m k v] (update-in m [k] #(conj % v)))]
          ["-s" "--resolve-strategy STRATEGY"
           "Specify a strategy to use when resolving. May be 'fast' or 'thorough'."
           :default "thorough"
          :validate [#(or (= "thorough" %) (= "fast" %))
                     "Strategy must either be 'thorough' or 'fast'."]]
          ["-R" "--repo-merge-strategy STRATEGY"
           "Specify a repo merge strategy. May be 'priority' or 'global'."
           :default "priority"
           :validate [#(or (= "priority" %) (= "global" %))
                      "Strategy must either be 'priority' or 'global'."]]
          ["-c" "--project-file PROJECT_FILE"
           "Resolve the requirements found in the given degasolv project file."
          :validate [#(fs/file? %) "Project file must exist."]]]}})

(defn command-list [commands]
  (->> ["Commands are:"
        ""
        (string/join \newline (map #(str "  - " %) commands))
        ""
        "Simply run `degasolv <command> -h` for help information."
        ""
        ]
       (string/join \newline)))

(defn errors [errors usg]
  (string/join \newline
               "Errors:"
               ""
               (map #(str "  - " %) errors)
               ""
               usg
               ""
               ""))

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
          "Options are shown below, with their default values and"
          "  descriptions:"
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
       (string/join \newline (map #(str "  - " %) errors))))

(defn exit [status msg]
  (.println *err* msg)
  (System/exit status))

(def cli-options
  [["-c" "--config-file FILE" "config file"
    :default (fs/file (fs/expand-home "~/.config/degasolv/config.edn"))
    :default-desc "~/.config/degasolv/config.edn"
    :validate [#(and (fs/exists? %)
                     (fs/file? %))
               "Must be a regular file (which hopefully contains config info."]]])

(defn -main [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args (concat
                          cli-options
                          [["-h" "--help" "Print this help page"]])
                    :in-order true)]
    (cond
      (:help options) (exit 0 (str (usage summary)
                                   "\n\n"
                                   (command-list (keys subcommand-cli))
                                   "\n\n"
                                   ))
      errors (exit 1 (string/join
                      \newline
                      [(error-msg errors)
                      ""
                      (usage summary)
                      ""
                       (command-list (keys subcommand-cli))
                       ""])))

    (let [global-options options
          subcommand (first arguments)
          subcmd-cli (get subcommand-cli subcommand)]
      (when (nil? subcmd-cli)
        (exit 1 (error-msg [(str "Unknown command: " subcommand)])))
      (let [{:keys [options arguments errors summary]}
            (parse-opts args (concat
                              (:cli subcmd-cli)
                              [["-h" "--help" "Print this help page"]]))]
        (cond
          (:help options) (exit 0 (usage summary :sub-command subcommand))
          errors (exit 1 (string/join
                          \newline
                          [(error-msg errors :sub-command subcommand)
                          ""
                          (usage summary :sub-command subcommand)
                          ""])))
        ((:function subcmd-cli)
         (merge
          (edn/read-string
           (default-slurp
            (:config-file global-options)))
          options)
         arguments)))))
