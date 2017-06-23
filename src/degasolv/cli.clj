(ns degasolv.cli
  (:require [degasolv.util :refer :all]
            [degasolv.resolver :as r :refer :all]
            [degasolv.pkgsys.apt :as apt-pkg]
            [degasolv.pkgsys.core :as degasolv-pkg]
            [clojure.tools.cli :refer [parse-opts summarize]]
            [clojure.string :as string]
            [clojure.edn :as edn]
            [clojure.spec :as s]
            [clojure.set :as st]
            [me.raynes.fs :as fs]
            [miner.tagged :as tag]
            [clojure.pprint :as pprint]
            [serovers.core :as vers])
  (:gen-class))

(defmethod
  print-method
  degasolv.resolver.VersionPredicate
  [this w]
  (tag/pr-tagged-record-on this w))

(defmethod
  print-method
  degasolv.resolver.Requirement
  [this w]
  (tag/pr-tagged-record-on this w))

(defn- read-card!
  [card]
  (let [card-data (tag/read-string (default-slurp card))
        vetted-card-data
        (s/conform ::r/package card-data)]
    (if (= vetted-card-data
           ::s/invalid)
      (throw (ex-info (str
                        "Invalid data in card file `"
                        card
                        "`: "
                        (s/explain ::r/package
                                   card-data))
                      (s/explain-data ::r/package
                                      card-data)))
      card-data)))

(defn aggregator
  [index-strat cmp]
  (cond
    (= index-strat "priority")
    priority-repo
    (= index-strat "global")
    (fn [rs]
      global-repo rs
      :cmp #(- (cmp
                 (:version %1)
                 (:version %2))))))

(def
  ^:private
  package-systems
  {"apt" {:genrepo apt-pkg/slurp-apt-repo
             :vercmp vers/debian-vercmp}
   "degasolv" {:genrepo degasolv-pkg/slurp-degasolv-repo
               :vercmp vers/maven-vercmp}})

(defn- generate-repo-index!
  [options arguments]
  (let [{:keys [search-directory
                index-file
                add-to]} options
        output-file index-file
        initial-repository
        (if add-to
          (tag/read-string
            (default-slurp add-to))
          {})]
    (default-spit
      output-file
        (into {}
              (map
                (fn [x]
                  [(first x)
                   (into []
                         (sort #(- (vers/maven-vercmp (:version %1)
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
                            (file-seq (fs/file search-directory))))))))))


(defn exit [status msg]
  (.println *err* msg)
  (System/exit status))

(defn aggregate-repositories
  [index-strat
   pkgsys
   repositories]

  ((aggregator index-strat
               (get-in package-systems [pkgsys :vercmp]))
   (flatten
    (map
       (fn [url]
         ((get-in package-systems [pkgsys :genrepo]) url)
         )
       repositories))))

(defn-
  display-config!
  [options arguments]
  (pprint/pprint
   (assoc options :arguments arguments)))

(defn-
  resolve-locations!
  [options arguments]
  (let
      [{:keys [alternatives
               repositories
               resolve-strat
               conflict-strat
               index-strat
               present-packages
               requirements
               package-system]}
       options
       requirement-data
       (into []
             (map
               (fn [str-req]
                 (let [vetted-str-req
                       (s/conform ::r/requirement-string str-req)]
                   (when (= vetted-str-req ::s/invalid)
                     (binding [*out* *err*]
                       (println
                         (str
                           "Requirement `"
                           str-req
                           "` invalid:"
                           (s/explain ::r/requirement-string str-req)))))
                   (string-to-requirement vetted-str-req)))
               requirements))
       present-packages
       (into {}
             (map
               (fn [str-pkg]
                 (let [vetted-str-pkg
                       (s/conform ::r/frozen-package-string str-pkg)]
                   (when (= vetted-str-pkg ::s/invalid)
                     (binding [*out* *err*]
                       (println
                         (str
                           "Present package string `"
                           str-pkg
                           "` invalid:"
                           (s/explain ::r/frozen-package-string str-pkg)))))
                   (let [[id version] (string/split str-pkg #"==")]
                     [id
                      (->PackageInfo
                        id
                        version
                        "already present"
                        nil)]))))
         (:present-packages options))
       aggregate-repo
       (aggregate-repositories
         index-strat
         package-system
         repositories)
       result
       (resolve-dependencies
        requirement-data
        aggregate-repo
        :present-packages present-packages
        :strategy (keyword resolve-strat)
        :conflict-strat (keyword conflict-strat)
        :compare (get-in package-systems [package-system :vercmp])
        :allow-alternatives alternatives)]
    (case
      (first result)
      :successful
      (let [[_ packages] result]
        (println (string/join
                  \newline
                  (map
                    explain-package
                    packages))))
      :unsuccessful
      (let [[_ info] result]
        (exit 1
              (string/join
               \newline
               (into
                [""
                 ""
                 "Could not resolve dependencies."
                 ""
                 ""
                 "The resolver encountered the following problems: "]
                (map r/explain-problem (:problems info)))))))))

(defn- generate-card!
  [{:keys [id version location requirements card-file]}
   arguments]
  (default-spit
   card-file
   (->PackageInfo
    id
    version
    location
    (into []
          (map
           #(string-to-requirement %)
           requirements)))))

(defn query-repo!
  [options arguments]
  (let [{:keys [repositories query index-strat package-system]} options
        aggregate-repo
        (aggregate-repositories
          index-strat
          package-system
          repositories)
        req (first (string-to-requirement query))
        {:keys [id spec]} req
        spec-call (make-spec-call
                   (get-in package-systems
                           [package-system :vercmp]))
        results (filter
                 #(spec-call spec %)
                 (aggregate-repo id))]
    (if (empty? results)
      (exit 2 "No results returned from query")
      (println
       (string/join
        \newline
        (map
         explain-package
         results))))))

(def subcommand-cli
  {"display-config"
   {:description "Print the effective combined configuration (and arguments) of all the given config files."
    :function display-config!
    }
   "generate-card"
   {:description "Generate dscard file based on arguments given"
    :function generate-card!
    :required-arguments {:id ["-i" "--id"]
                         :version ["-v" "--version"]
                         :location ["-l" "--location"]}
    :cli [["-i" "--id ID"
           "ID (name) of the package"
           :validate [#(not (empty? %))
                      "ID must be a non-empty string."]
           :required true]
          ["-v" "--version VERSION"
           "Version of the package"
           :validate [#(re-matches r/version-regex %)
                      "Sorry, given argument doesn't look like a version."]
           :required true]
          ["-l" "--location LOCATION"
           "URL or filepath of the package"
           :validate [#(not (empty? %))
                      "Location must be a non-empty string."]
           :required true]
          ["-r" "--requirement REQ"
           "List req, may be used multiple times"
           :validate [#(re-matches r/str-requirement-regex %)
                      "Requirement must look like one of these: `!a`, `a`, `a|b`, a>2.0,<=3.0,!=2.5;>4.0,<=5.0`"]
           :id :requirements
           :assoc-fn
           (fn [m k v] (update-in m [k] #(conj % v)))]
          ["-C" "--card-file FILE"
           (str "The name of the card file")
           :default "./out.dscard"
           :validate [#(not (empty? %))
                      "Out file must not be empty."]]]}
   "generate-repo-index"
   {:description "Generate repository index based on degasolv package cards"
    :function generate-repo-index!
    :cli [["-d" "--search-directory DIR" "Find degasolv cards here"
           :default "."
           :validate [#(and
                        (fs/directory? %)
                        (fs/exists? %))
                      "Must be a directory which exists on the file system."]]
          ["-I" "--index-file FILE"
           "The name of the repo file"
           :default "index.dsrepo"]
          ["-a" "--add-to INDEX"
           "Add to repo index INDEX"]]}

   "resolve-locations"
   {:description "Print the locations of the packages which will resolve all given dependencies."
    :function resolve-locations!
    :required-arguments {:repositories ["-R" "--repository"]
                         :requirements ["-r" "--requirement"]}
    :cli [
          ["-a" "--enable-alternatives" "Consider all alternatives"
           :id :alternatives
           :default true
           :assoc-fn (fn [m k v] (assoc m :alternatives v))]
          ["-A" "--disable-alternatives" "Consider only first alternatives"
           :parse-fn not
           :assoc-fn (fn [m k v] (assoc m :alternatives v))]
          ["-f" "--conflict-strat STRAT"
           "May be 'exclusive', 'inclusive' or 'prioritized'."
           :default "exclusive"
           :validate [#(or (= "exclusive" %)
                           (= "inclusive" %)
                           (= "prioritized" %))
                      "Conflict strategy must either be 'exclusive', 'inclusive', or 'prioritized'."]]
          ["-p" "--present-package PKG"
           "Hard present package. **"
           :id :present-packages
           :validate
           [#(re-matches r/str-frozen-package-regex %)
            "Package must be specified as `<pkgname>==<pkgversion>`"]
           :assoc-fn
           (fn [m k v] (update-in m [k] #(conj % v)))]
          ["-r" "--requirement REQ"
           "Resolve req. **"
           :id :requirements
           :validate
           [#(re-matches r/str-requirement-regex %)
            "Requirement must look like one of these: `!a`, `a`, `a|b`, a>2.0,<=3.0,!=2.5;>4.0,<=5.0`"]
           :id :requirements
           :assoc-fn
           (fn [m k v] (update-in m [k] #(conj % v)))]
          ["-R" "--repository INDEX"
           "Search INDEX for packages. **"
           :id :repositories
           :assoc-fn
           (fn [m k v] (update-in m [k] #(conj % v)))]
          ["-s" "--resolve-strat STRAT"
           "May be 'fast' or 'thorough'."
           :default "thorough"
           :validate [#(or (= "thorough" %) (= "fast" %))
                      "Resolve strategy must either be 'thorough' or 'fast'."]]
          ["-S" "--index-strat STRAT"
           "May be 'priority' or 'global'."
           :default "priority"
           :validate [#(or (= "priority" %) (= "global" %))
                      "Strategy must either be 'priority' or 'global'."]]
          ["-t" "--package-system SYS"
           "May be 'degasolv' or 'apt'."
           :default "degasolv"
           :validate [#(or (= "degasolv" %) (= "apt" %))
                      "Package system must be either 'degasolv' or 'apt'."]]]}
   "query-repo"
   {:description "Query repository for a particular package"
    :function query-repo!
    :required-arguments {:repositories ["-R" "--repository"]
                         :query ["-q" "--query"]}
    :cli [["-q" "--query QUERY"
           "Display packages matching query string."
           :validate [#(and (re-matches r/str-requirement-regex %)
                            (let [strreq (string-to-requirement %)]
                              (and (= (count strreq) 1)
                                   (= (:status (get strreq 0)) :present))))
                      "Query must look like one of these: `a`, `a`, a>2.0,<=3.0,!=2.5;>4.0,<=5.0`"]]
          ["-R" "--repository INDEX"
           "Search INDEX for packages. **"
           :id :repositories
           :assoc-fn
           (fn [m k v] (update-in m [k] #(conj % v)))]
          ["-S" "--index-strat STRAT"
           "May be 'priority' or 'global'."
           :default "priority"
           :validate [#(or (= "priority" %) (= "global" %))
                      "Strategy must either be 'priority' or 'global'."]]
          ["-t" "--package-system SYS"
           "May be 'degasolv' or 'apt'."
           :default "degasolv"
           :validate [#(or (= "degasolv" %) (= "apt" %))
                      "Package system must be either 'degasolv' or 'apt'."]]]}})

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
          "  descriptions. Options marked with `**` may be"
          "  used more than once."
          ""
          options-summary
          ]
         (string/join \newline))))

(defn required-args-msg [required-args & {:keys [sub-command]}]
   (str "The following options are required"
        (if sub-command
          (str
           " for subcommand `" sub-command "`"
          ""))
        ":\n\n"
        (string/join
         \newline
         (map
          (fn format-argument
            [[k [small-arg large-arg]]]
            (str "  - `"
                 small-arg
                 "`, `"
                 large-arg
                 "`, or the config file key `"
                 k
                 "`."))
          required-args))))

(defn error-msg [errors & {:keys [sub-command]}]
  (str "The following errors occurred while parsing commandline options"
       (if sub-command
         (str " for subcommand `" sub-command "`")
         "")
       ":\n\n"
       (string/join \newline (map #(str "  - " %) errors))))

(defn missing-required-argument
  [required-arguments
   missing-key]
  (let [[small-arg large-arg] (missing-key required-arguments)]
    (string/join
     \newline
     [""
      (str "Missing argument `"
           (name missing-key)
           "`.")
      (str "  To specify it, either use the `"
           (str missing-key)
           "` key in the config file,")
      (str "  or use `" small-arg "` or `" large-arg "` at the command line.")])))

(def cli-options
  [["-c" "--config-file FILE" "config file"
    :id :config-files
    :default []
    :default-desc "./degasolv.edn"
    :validate [#(and (fs/exists? %)
                     (fs/file? %))
               "Must be a regular file (which hopefully contains config info."]
    :assoc-fn
    (fn [m k v] (update-in m [k] #(conj % v)))]])

(defn- deep-merge [a b]
  (merge-with (fn [x y]
                (cond (map? y) (deep-merge x y)
                      (vector? y) (conj x y)
                      :else y))
              a b))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args (concat
                          cli-options
                          [["-h" "--help" "Print this help page"]])
                    :in-order true)]
    (cond
      (:help options)
      (exit 0
            (str (usage summary)
                 (if (:required-arguments cli-options)
                   (str
                    "\n\n"
                    (required-args-msg
                     (:required-arguments cli-options))
                    "\n\n")
                   "\n\n")
                 (command-list (keys subcommand-cli))
                 "\n\n"))
      errors
      (exit 1
            (string/join
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
            (parse-opts arguments (concat
                                   (:cli subcmd-cli)
                                   [["-h" "--help" "Print this help page"]]))]
        (cond
          (:help options)
          (exit 0
                (str (usage
                      summary
                      :sub-command
                      subcommand)
                     (if (:required-arguments subcmd-cli)
                       (str
                        "\n\n"
                        (required-args-msg
                         (:required-arguments subcmd-cli)
                         :sub-command subcommand)
                        "\n\n")
                       "\n\n")))
          errors (exit 1 (string/join
                          \newline
                          [(error-msg errors :sub-command subcommand)
                           ""
                           (usage summary :sub-command subcommand)
                           ""])))
        (let [config-files (if (empty? (:config-files global-options))
                             [(fs/file (fs/expand-home "./degasolv.edn"))]
                             (:config-files global-options))
              effective-options
              (merge
               (try
                 (reduce
                  merge
                  (map
                  tag/read-string
                  (map
                   default-slurp
                   (:config-files global-options))))
                 (catch Exception e
                   (binding [*out* *err*]
                     (println "Warning: problem reading config files, they were not used:"
                              (str (string/join
                                    \newline
                                    (map #(str "  - " %)
                                         (:config-files global-options))))))
                   {}))
               options)
              required-keys (set (keys (:required-arguments subcmd-cli)))
              present-keys (set (keys effective-options))]
          (when (not (st/subset? required-keys present-keys))
            (exit 1
                  (string/join
                   \newline
                   (map (partial missing-required-argument
                                 (:required-arguments subcmd-cli))
                        (st/difference required-keys present-keys)))))
          ((:function subcmd-cli)
           effective-options
           arguments))))))
