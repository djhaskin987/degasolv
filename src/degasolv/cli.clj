(ns degasolv.cli
  (:require
   [clojure.data.json :as json]
   [clojure.edn :as edn]
   [clojure.pprint :as pprint]
   [clojure.set :as st]
   [clojure.spec :as s]
   [clojure.string :as string]
   [clojure.tools.cli :refer [parse-opts summarize]]
   [degasolv.pkgsys.apt :as apt-pkg]
   [degasolv.pkgsys.core :as degasolv-pkg]
   [degasolv.pkgsys.subproc :as subproc-pkg]
   [degasolv.resolver :as r :refer :all]
   [degasolv.util :refer :all]
   [me.raynes.fs :as fs]
   [miner.tagged :as tag]
   [serovers.core :as vers]
   [tupelo.core :as t])
  (:gen-class))

(defn- exit [status msg]
  (.println *err* msg)
  (System/exit status))

(defn- out-exit [status msg]
  (println msg)
  (System/exit status))

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
  version-comparators
  {
   "debian" vers/debian-vercmp
   "maven" vers/debian-vercmp
   "naive" vers/naive-vercmp
   "python" vers/python-vercmp
   "rpm" vers/rpm-vercmp
   "rubygem" vers/rubygem-vercmp
   "semver" vers/semver-vercmp
   })

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

(defn usage [command-name options-summary & {:keys [sub-command]}]
  (let [display-command (if sub-command
                          sub-command
                          "<command>")]
    (->> [(str "Usage: "
               command-name
               " <options> "
               (when sub-command
                 (str
                  sub-command
                  " <"
                  sub-command
                  "-options>")))
          ""
          "Options are shown below. Default values are listed with the"
          "  descriptions. Options marked with `**` may be"
          "  used more than once."
          ""
          options-summary
          ]
         (string/join \newline))))

(def
  ^:private
  package-systems
  {"apt" {:genrepo apt-pkg/slurp-apt-repo
             :version-comparison "debian"}
   "degasolv" {:genrepo degasolv-pkg/slurp-degasolv-repo
               :version-comparison "maven"}
   "subproc" {:constructor subproc-pkg/make-slurper
              :required-arguments {:subproc-exe ["-x" "--subproc-exe"]}}})

(defn command-list [commands]
  (->> ["Commands are:"
        ""
        (string/join \newline (map #(str "  - " %) commands))
        ""
        "Simply run `degasolv <command> -h` for help information."
        ""
        ]
       (string/join \newline)))

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

(defn- check-required! [options command-spec]
  (let [required-args (:required-arguments command-spec)
        required-keys (set (keys required-args))
        present-keys (set (keys options))]
    (when (not (st/subset? required-keys
                           present-keys))
               (exit 1
                     (string/join
                      \newline
                      (map (partial missing-required-argument
                                    required-args)
                           (st/difference required-keys present-keys)))))))

(defn- parseplz!
  [command args command-spec]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args (concat
                          (:cli command-spec)
                          [["-h" "--help" "Print this help page"]])
                    :in-order true)]
    (cond
      (or
       (:help options)
       (and
         (empty? arguments)
         (:subcommands command-spec)))
      (exit 1
            (str (usage command summary)
                 (if (:required-arguments command-spec)
                   (str
                    "\n\n"
                     (required-args-msg
                       (:required-arguments command-spec))
                     "\n\n")
                   "\n\n")
                 (if (:subcommands command-spec)
                   (str
                    (command-list (keys (:subcommands command-spec)))
                    "\n\n")
                   "")))
      errors
      (exit 1
            (string/join
             \newline
             [(error-msg errors)
              ""
              (usage command summary)
              (if (:subcommands command-spec)
                (string/join
                 \newline
                 [
                 ""
                 (command-list (keys (:subcommands command-spec)))
                 ""])
                "")])))
    {:options options
     :arguments arguments}))

(defn- generate-repo-index-cli!
  [options arguments]
  (let [{:keys [search-directory
                index-file
                version-comparison
                add-to]} options]
    (degasolv-pkg/generate-repo-index!
      search-directory
      index-file
      add-to
      (get version-comparators version-comparison))))

(defn- aggregate-repositories
  [index-strat
   repositories
   genrepo
   cmp]
  ((aggregator index-strat
               cmp)
   (flatten
    (map
       (fn [url]
         (genrepo url))
       repositories))))

(defn-
  display-config!
  [options arguments]
  (pprint/pprint
   (assoc options :arguments arguments)))

(defn- resolver-error
  [problems]
  (string/join
   \newline
   (into
    [""
     ""
     "Could not resolve dependencies."
     ""
     ""
     "The resolver encountered the following problems: "]
    (map r/explain-problem problems))))

(defn-
  resolve-locations!
  [options arguments]
  (let [{:keys [alternatives
                conflict-strat
                index-strat
                error-format
                output-format
                package-system
                present-packages
                repositories
                requirements
                resolve-strat
                search-strat
                version-comparison]}
       options]
    (when (get-in package-systems [package-system :required-arguments])
      (check-required! options (get package-systems package-system)))
    (let [genrepo
          (if (get-in package-systems [package-system :constructor])
            ((get-in package-systems [package-system :constructor])
             options)
            (get-in package-systems [package-system :genrepo]))
          version-comparator
          (get version-comparators version-comparison)
          requirement-data
          (mapv
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
           requirements)
          present-packages
          (reduce
           (fn package-aggregate
             [c [name pkg]]
             (update-in c [name]
                        conj
                        pkg))
           {}
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
                    nil)])))
            (:present-packages options)))
          aggregate-repo
          (aggregate-repositories
           index-strat
           repositories
           genrepo
           version-comparator)
          result
          (resolve-dependencies
           requirement-data
           aggregate-repo
           :present-packages present-packages
           :strategy (keyword resolve-strat)
           :conflict-strat (keyword conflict-strat)
           :search-strat (keyword search-strat)
           :compare version-comparator
           :allow-alternatives alternatives)
          base-result-info
          {
           :command "degasolv"
           :subcommand "resolve-locations"
           :options options
           :result (first result)
           }
          result-info
          (if  (= (first result) :successful)
            (into base-result-info
                  {:packages (second result)})
            (into base-result-info (second result)))]
      (if (= (first result) :successful)
        (println
         (case output-format
           "json"
           (json/write-str result-info :escape-slash false)
           "edn"
           (pr-str result-info)
           "plain"
           (string/join
            \newline
            (map explain-package (:packages result-info)))
           (throw (ex-info "This shouldn't happen"
                           {:subcommand "resolve-locations"
                            :output-format output-format
                            :result (first result)}))))
        (if error-format
          (out-exit 1
                    (case output-format
                      "json"
                      (json/write-str result-info :escape-slash false)
                      "edn"
                      (pr-str result-info)
                      "plain"
                      (resolver-error (:problems result-info))
                      (throw (ex-info "This shouldn't happen"
                                      {:subcommand "resolve-locations"
                                       :output-format output-format
                                       :result (first result)}))))
          (exit 1 (resolver-error (:problems result-info))))))))

(defn- generate-card!
  [{:keys [id version location requirements card-file meta]}
   arguments]
  (default-spit
   card-file
   (into
    (->PackageInfo
    id
    version
    location
    (into []
          (map
           #(string-to-requirement %)
           requirements)))
    (dissoc meta :id :version :location :requirements))))

(defn query-repo!
  [options arguments]
  (let [{:keys [index-strat
                error-format
                output-format
                package-system
                repositories
                query
                version-comparison]}
        options
        version-comparator
        (get version-comparators version-comparison)
        aggregate-repo
        (aggregate-repositories
         index-strat
         repositories
         (get-in package-systems [package-system :genrepo])
         version-comparator)
        req (first (string-to-requirement query))
        {:keys [id spec]} req
        spec-call (make-spec-call
                   version-comparator)
        results (filter
                 #(spec-call spec %)
                 (aggregate-repo id))
        result-info
        {
         :command "degasolv"
         :subcommand "resolve-locations"
         :options options
         :packages results
         }]
    (if (empty? results)
      (if error-format
        (out-exit 2
                  (case output-format
                    "json"
                    (json/write-str result-info :escape-slash false)
                    "edn"
                    (pr-str result-info)
                    "plain"
                    "No results returned from query"
                    (throw (ex-info "This shouldn't happen"
                                    {:subcommand "query-repo"
                                     :output-format output-format
                                     :result :unsuccessful}))))
        (exit 2 "No results returned from query"))
        (println
         (case output-format
           "json"
           (json/write-str result-info :escape-slash false)
           "edn"
           (pr-str result-info)
           "plain"
           (string/join
            \newline
            (map
             explain-package
             results))
           (throw (ex-info "This shouldn't happen"
                           {:subcommand "query-repo"
                            :output-format output-format
                            :result :successful})))))))

(def subcommand-option-defaults
  {
   :alternatives true
   :error-format false
   :card-file "./out.dscard"
   :conflict-strat "exclusive"
   :index-file "index.dsrepo"
   :index-strat "priority"
   :output-format "plain"
   :subproc-output-format "json"
   :package-system "degasolv"
   :resolve-strat "thorough"
   :search-directory "."
   :search-strat "breadth-first"
   :subproc-out-format "json"
   })

(def available-option-packs
  {
   "multi-version-mode"
   {
    :conflict-strat "inclusive"
    :resolve-strat "fast"
    :alternatives false
    }
    "firstfound-version-mode"
   {
    :conflict-strat "prioritized"
    :resolve-strat "fast"
    :alternatives false
    }
  })

(def cli-spec
  {
   :description "Degasolv: You love me."
   :cli
   [["-c" "--config-file FILE" "Config file location **"
     :id :config-files
     :default []
     :default-desc "./degasolv.edn"
     :assoc-fn
     (fn [m k v] (update-in m [k] #(conj % v)))]
    ["-k" "--option-pack PACK" "Specify option pack **"
     :id :option-packs
     :default []
     :default-desc ""
     :validate [#(get available-option-packs %)
                (str
                 "Must be one of: "
                 (string/join "," (keys available-option-packs)))]
     :assoc-fn
     (fn [m k v] (update-in m [k] #(conj % v)))]]
   :subcommands
   {"display-config"
    {
     :description "Print the effective combined configuration (and arguments) of all the given config files."
     :function display-config!
     }
    "generate-card"
    {
     :description "Generate dscard file based on arguments given"
     :function generate-card!
     :required-arguments {:id ["-i" "--id"]
                          :version ["-v" "--version"]
                          :location ["-l" "--location"]}
     :cli [
           ["-C" "--card-file FILE"
            "The name of the card file"
            :default nil
            :default-desc (str (:card-file subcommand-option-defaults))
            :validate [#(not (empty? %))
                       "Out file must not be empty."]]
           ["-i" "--id ID"
            "ID (name) of the package"
            :validate [#(not (empty? %))
                       "ID must be a non-empty string."]
            :required true]
           ["-l" "--location LOCATION"
            "URL or filepath of the package"
            :validate [#(not (empty? %))
                       "Location must be a non-empty string."]
            :required true]
           ["-m" "--meta K=V"
            "Add additional metadata"
            :validate [#(re-matches #"^[^=]+=[^=].*$" %)
                       "Metadata must be presented as <key>=<value> pair."]
            :id :meta
            :assoc-fn
            (fn [m k v]
              (let [[_ rk rv] (re-find #"^([^=]+)=([^=].*)$" v)]
                (update-in m [k] #(assoc % (keyword rk) rv))))]
           ["-r" "--requirement REQ"
            "List requirement **"
            :validate [#(re-matches r/str-requirement-regex %)
                       "Requirement must look like one of these: `!a`, `a`, `a|b`, a>2.0,<=3.0,!=2.5;>4.0,<=5.0`"]
            :id :requirements
            :assoc-fn
            (fn [m k v] (update-in m [k] #(conj % v)))]
           ["-v" "--version VERSION"
            "Version of the package"
            :validate [#(re-matches r/version-regex %)
                       "Sorry, given argument doesn't look like a version."]
            :required true]
           ]}
    "generate-repo-index"
    {:description "Generate repository index based on degasolv package cards"
     :function generate-repo-index-cli!
     :cli [["-a" "--add-to INDEX"
            "Add to repo index INDEX"]
           ["-d" "--search-directory DIR" "Find degasolv cards here"
            :default nil
            :default-desc (str (:search-directory subcommand-option-defaults))
            :validate [#(and
                         (fs/directory? %)
                         (fs/exists? %))
                       "Must be a directory which exists on the file system."]]
           ["-I" "--index-file FILE"
            "The name of the repo file"
            :default nil
            :default-desc (str (:index-file subcommand-option-defaults))]
           ["-V" "--version-comparison CMP"
            "May be 'debian', 'maven', 'naive', 'python', 'rpm', 'rubygem', or 'semver'."
            :default nil
            :default-desc "maven"
            :validate [#(some #{%} (keys version-comparators))
                       "Version comparison must be 'debian', 'maven', 'naive', 'python', 'rubygem', or 'semver'."]]]}

    "resolve-locations"
    {:description "Print the locations of the packages which will resolve all given dependencies."
     :function resolve-locations!
     :required-arguments {:repositories ["-R" "--repository"]
                          :requirements ["-r" "--requirement"]}
     :cli [
           ["-a" "--enable-alternatives" "Consider all alternatives (default)"
            :assoc-fn (fn [m k v] (assoc m :alternatives true))]
           ["-A" "--disable-alternatives" "Consider only first alternatives"
            :assoc-fn (fn [m k v] (assoc m :alternatives false))]
           ["-e" "--search-strat STRAT"
            "May be 'breadth-first' or 'depth-first'."
            :default nil
            :default-desc (str (:search-strat subcommand-option-defaults))
            :validate [#(or (= "breadth-first" %)
                            (= "depth-first" %))
                       "Search strategy must either be 'breadth-first' or 'depth-first'."]]
           ["-g" "--enable-error-format" "Enable output format for errors"
            :assoc-fn (fn [m k v] (assoc m :error-format true))]
           ["-G" "--disable-error-format" "Disable output format for errors (default)"
            :assoc-fn (fn [m k v] (assoc m :error-format false))]
           ["-f" "--conflict-strat STRAT"
            "May be 'exclusive', 'inclusive' or 'prioritized'."
            :default nil
            :default-desc (str (:conflict-strat subcommand-option-defaults))
            :validate [#(or (= "exclusive" %)
                            (= "inclusive" %)
                            (= "prioritized" %))
                       "Conflict strategy must either be 'exclusive', 'inclusive', or 'prioritized'."]]
           ["-o" "--output-format FORMAT" "May be 'plain', 'edn' or 'json'"
            :default nil
            :default-desc (str (:output-format subcommand-option-defaults))
            :validate [#(or (= "plain" %)
                            (= "json" %)
                            (= "edn" %))
                       "Output format may be either be 'plain', 'edn' or 'json'"]]
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
            :assoc-fn
            (fn [m k v] (update-in m [k] #(conj % v)))]
           ["-R" "--repository INDEX"
            "Search INDEX for packages. **"
            :id :repositories
            :assoc-fn
            (fn [m k v] (update-in m [k] #(conj % v)))]
           ["-s" "--resolve-strat STRAT"
            "May be 'fast' or 'thorough'."
            :default nil
            :default-desc (str (:resolve-strat subcommand-option-defaults))
            :validate [#(or (= "thorough" %) (= "fast" %))
                       "Resolve strategy must either be 'thorough' or 'fast'."]]

           ["-S" "--index-strat STRAT"
            "May be 'priority' or 'global'."
            :default nil
            :default-desc (str (:index-strat subcommand-option-defaults))
            :validate [#(or (= "priority" %) (= "global" %))
                       "Strategy must either be 'priority' or 'global'."]]
           ["-t" "--package-system SYS"
            "May be 'degasolv', 'apt', or 'subproc'."
            :default nil
            :default-desc (str (:package-system subcommand-option-defaults))
            :validate [#(or (= "degasolv" %)
                            (= "apt" %)
                            (= "subproc" %))
                       "Package system must be either 'degasolv', 'apt', or 'subproc'."]]
           ["-V" "--version-comparison CMP"
            "May be 'debian', 'maven', 'naive', 'python', 'rpm', 'rubygem', or 'semver'."
            :default nil
            :default-desc "maven"
            :validate [#(some #{%} (keys version-comparators))
                       "Version comparison must be 'debian', 'maven', 'naive', 'python', 'rubygem', or 'semver'."]]
           ["-x" "--subproc-exe PATH"
            "Path to the executable to call to get package data"
            :validate [#(and
                         (fs/exists? %)
                         (fs/executable? %))
                       "Must be an executable file which exists on the file system."]]
           ["-u" "--subproc-output-format"
            "Whether to read `edn` or `json` from the exe's output"
            :default nil
            :default-desc "json"
            :validate [#(or (= "json" %)
                            (= "edn" %))
                       "Subproc output format may be either be 'edn' or 'json'"]]
           ]}
    "query-repo"
    {:description "Query repository for a particular package"
     :function query-repo!
     :required-arguments {:repositories ["-R" "--repository"]
                          :query ["-q" "--query"]}
     :cli [
           ["-g" "--enable-error-format" "Enable output format for errors"
            :assoc-fn (fn [m k v] (assoc m :error-format true))]
           ["-G" "--disable-error-format" "Disable output format for errors (default)"
            :assoc-fn (fn [m k v] (assoc m :error-format false))]
           ["-o" "--output-format FORMAT" "May be 'plain', 'edn' or 'json'"
            :default nil
            :default-desc (str (:output-format subcommand-option-defaults))
            :validate [#(or (= "plain" %)
                            (= "json" %)
                            (= "edn" %))
                       "Output format may be either be 'plain', 'edn' or 'json'"]]
           ["-q" "--query QUERY"
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
            :default nil
            :default-desc (str (:index-strat subcommand-option-defaults))
            :validate [#(or (= "priority" %) (= "global" %))
                       "Strategy must either be 'priority' or 'global'."]]
           ["-t" "--package-system SYS"
            "May be 'degasolv' or 'apt'."
            :default nil
            :default-desc (str (:package-system subcommand-option-defaults))
            :validate [#(or (= "degasolv" %) (= "apt" %))
                       "Package system must be either 'degasolv' or 'apt'."]]
           ["-V" "--version-comparison CMP"
            "May be 'debian', 'maven', 'naive', 'python', 'rpm', 'rubygem', or 'semver'."
            :default nil
            :default-desc "maven"
            :validate [#(some #{%} (keys version-comparators))
                       "Version comparison must be 'debian', 'maven', 'naive', 'python', 'rubygem', or 'semver'."]]
           ]
     }
    }
   }
  )



(defn errors [errors usg]
  (string/join \newline
               "Errors:"
               ""
               (map #(str "  - " %) errors)
               ""
               usg
               ""
               ""))







(defn get-config [configs]
  (try
    (reduce
     merge
     (map
      tag/read-string
      (map
       default-slurp
       configs)))
    (catch Exception e
      (binding [*out* *err*]
        (println "Warning: problem reading config files, they were not used:"
                 (str "\n"
                      (string/join
                       \newline
                       (map #(str "  - " %)
                            configs)))))
      (hash-map))))



(defn -main [& args]
  (let [{:keys [options arguments]}
        (parseplz! "degasolv" args cli-spec)]
    (let [global-options options
          subcommand (first arguments)
          subcommand-cli (:subcommands cli-spec)
          subcmd-cli (if (not (= subcommand "display-config"))
                       (get subcommand-cli subcommand)
                       ; this grabs all other options as part of display-config
                       (t/it-> subcommand-cli
                               (vals it)
                               (map :cli it)
                               (filter #(not (nil? %)) it)
                               (apply concat it)
                               (map #(concat [nil] (subvec % 1)) it)
                               (map #(do [(second %) %]) it)
                               (into {} it)
                               (vals it)
                               (assoc
                                (get subcommand-cli subcommand)
                                :cli
                                it)
                               it))]
      (when (nil? subcmd-cli)
        (exit 1 (error-msg [(str "Unknown command: " subcommand)])))
      (let [{:keys [options arguments]}
            (parseplz! subcommand (rest arguments) (get subcommand-cli subcommand))]
        (let [config-files
              (if (empty? (:config-files global-options))
                [(fs/file (fs/expand-home "./degasolv.edn"))]
                (:config-files global-options))
              config
              (get-config config-files)
              cli-option-packs (:option-packs global-options)
              selected-option-packs
              (if (empty? cli-option-packs)
                (into [] (:option-packs config))
                cli-option-packs)
              effective-options
              (t/it->
               selected-option-packs
               (mapv available-option-packs it)
               (into [subcommand-option-defaults] it)
               (conj it (dissoc config :option-packs))
               (conj it (into {}
                              (filter
                               #(not (nil? (second %)))
                               (seq options))))
               (reduce merge (hash-map) it)
               (if (not (:version-comparison it))
                 (assoc
                  it
                  :version-comparison
                  (if (get-in package-systems
                              [(:package-system it) :version-comparison])
                    (get-in package-systems
                            [(:package-system it) :version-comparison])
                    (get-in package-systems
                            [(:package-system subcommand-option-defaults)
                             :version-comparison])))
                 it))]
          (check-required! effective-options subcmd-cli)
          ((:function subcmd-cli)
           effective-options
           arguments))))))
