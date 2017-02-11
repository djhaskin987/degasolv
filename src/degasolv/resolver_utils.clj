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
    (let [result (find m nm)]
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
