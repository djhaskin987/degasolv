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
              initial-term {:status (if (empty? status-piece)
                                      :present
                                      :absent)
                            :id name-piece}]
          (if (empty? spec-piece)
            initial-term
            (into
             initial-term
             [[:spec
               (into []
                     (map
                      (fn [t]
                        (into
                         []
                         (map
                          (fn [rough]
                            (let [[_ cse version]
                                  (re-find #"(<|<=|!=|==|>=|>)([^<>=!].*)$" rough)]
                              {:relation (case cse
                                           "<" :less-than
                                           "<=" :less-equal
                                           "==" :equal-to
                                           "!=" :not-equal
                                           ">=" :greater-equal
                                           ">" :greater-than)
                               :version version}))
                          (clj-str/split t #","))))
                      (clj-str/split spec-piece #";")))]]))))
      (clj-str/split str #"\|")))))
