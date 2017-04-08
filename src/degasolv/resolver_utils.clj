(in-ns 'degasolv.resolver)

(defn priority-repo [rs]
  (fn [id]
    (or
      (first
        (filter
          #(not (empty? %))
          (map #(% id)
               rs)))
      [])))

(defn global-repo [rs & {:keys [cmp]
                         :or {cmp #(- (compare (:version %1) (:version %2)))}}]
  (fn [id]
    (or
     (sort #(cmp %1 %2)
           (flatten (map #(% id) rs)))
     [])))

(defn map-query [m]
  (fn [nm]
    (dbg nm)
    (dbg (get m nm))
    (let [result (find m nm)]
      (dbg result)
      (if (nil? result)
        []
        (let [[k v] result]
          v)))))

(defn string-to-requirement
  [str]
  (if (empty? str)
    []
    (into
     []
     (map
      (fn extract-name [x]
        (let [[status-piece name-piece spec-piece]
              (rest (re-find #"^(!?)([^!><=]+)(.*)$" x))
              initial-term (if (empty? status-piece)
                             (present name-piece)
                             (absent name-piece))]
          (if (empty? spec-piece)
            initial-term
            (assoc
              initial-term
              :spec
              (into []
                     (map
                      (fn [t]
                        (into
                         []
                         (map
                          (fn [rough]
                            (let [[_ cse version]
                                  (re-find #"(<|<=|!=|==|>=|>)([^<>=!].*)$" rough)]
                              (->VersionPredicate
                                (case cse
                                  "<" :less-than
                                  "<=" :less-equal
                                  "==" :equal-to
                                  "!=" :not-equal
                                  ">=" :greater-equal
                                  ">" :greater-than)
                                version)))
                          (clj-str/split t #","))))
                      (clj-str/split spec-piece #";")))))))
      (clj-str/split str #"\|")))))

(defn explain-package [pkg]
  (str (:id pkg) "==" (:version pkg) " @ " (:location pkg)))

(defn explain-absent-spec [[id specs]]
  (str id (if (empty? specs)
            ""
            (str "( "
                 (clj-str/join
                  " "
                  (map #(str %) specs))
                 " )"))))

(defn explain-package-list [pkg-list label]
  (str "  - " label ":"
     (if (empty? pkg-list)
           " None"
           (clj-str/join
            \newline
            (into [""]
                  (map
                   #(str "    - " %)
                   (map explain-package pkg-list)))))))

(def ^:private reason-explanations
  {
   :uncovered-case "Unknown Cause (uncovered case in conditional)"
   :empty-alternative-set "Empty alternative set (e.g., the requirement \"|\")"
   :present-package-conflict "Package in question conflicts with a previously selected package."
   :package-not-found "Package in question is not present in the repository"
   :package-rejected "Package in question was found in the repository, but cannot be used."
   })

(defn explain-problem [problem]
  (clj-str/join
   \newline
   (concat
    [(str "  Clause: " (clj-str/join "|"
                                     (map
                                      str
                                      (:term problem))))
     (explain-package-list
      (vals (:found-packages problem))
      "Packages selected")]
    (when (not (nil? (:present-packages problem)))
      [(explain-package-list
       (vals (:present-packages problem))
       "Packages already present")])
    (when (not (nil? (:alternative problem)))
      [(str "  - Alternative being considered: " (:alternative problem))])
    (when (not (nil? (:reason problem)))
      [(str "  - " ((:reason problem) reason-explanations))])
    (when (not (nil? (:package-id problem)))
      [(str "  - Package ID in question: " (:package-id problem))]))))
