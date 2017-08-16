(in-ns 'degasolv.resolver)

(s/def ::relation #{:less-than
                    :less-equal :equal-to
                    :not-equal
                    :greater-equal
                    :greater-than})

(s/def ::id (s/and
              string?
              #(not (empty? %))))

(def str-version-pattern "[A-Za-z0-9][A-Za-z0-9]*([.-][A-Za-z0-9]+)*")

(def version-regex (re-pattern (str
                                 "^"
                                 str-version-pattern
                                 "$")))

(s/def ::version #(re-matches version-regex %))

(s/def ::location (s/and
                    string?
                    #(not (empty? %))))

(s/def ::status #{:present :absent})

(s/def ::version-predicate
       (s/keys :req-un [::relation ::version]))

(s/def ::version-conj-predicate
       (s/coll-of
         ::version-predicate
         :kind sequential?
         :into []
         :min-count 1))

(s/def ::spec
       (s/nilable
         (s/coll-of
           ::version-conj-predicate
           :kind sequential?
           :into [])))

(s/def ::alternative
       (s/keys :req-un [::status ::id] :opt-un [::spec]))

(s/def ::requirement
       (s/coll-of
         ::alternative
         :kind sequential?
         :into []
         :min-count 1
         :gen-max 4))

(s/def ::requirements
       (s/nilable
         (s/coll-of
           ::requirement
           :kind sequential?
           :into [])))

(s/def ::package
       (s/keys
         :req-un [::id ::version ::location]
         :opt-un [::requirements]))

(s/def ::map-repo
       (s/every-kv
         ::id
         (s/every
           ::package
           :into [])))

(def str-equals-pattern "==")
(def str-relation-pattern "(>=|==|!=|<=|<|>)")
(def str-id-pattern "[^>=<!;,|]+")
(def str-version-predicate-pattern
  (str str-relation-pattern
       str-version-pattern))
(def str-version-conj-predicate-pattern
  (str str-version-predicate-pattern
       "(,"
       str-version-predicate-pattern
       ")*"))
(def str-spec-pattern
  (str
    str-version-conj-predicate-pattern
    "(;"
    str-version-conj-predicate-pattern
    ")*"))

(def str-alternative-pattern
  (str
    "!?"
    str-id-pattern
    "("
    str-spec-pattern
    ")?"))
(def str-requirement-pattern
  (str
    str-alternative-pattern
    "([|]"
    str-alternative-pattern
    ")*"))

(def str-requirement-regex
 (re-pattern
  (str
   "^"
   str-requirement-pattern
   "$")))

(def str-frozen-package-pattern
  (str
    str-id-pattern
    "=="
    str-version-pattern))

(def str-frozen-package-regex
  (re-pattern
    (str
      "^"
      str-frozen-package-pattern
      "$")))
(s/def ::frozen-package-string
       #(re-matches str-frozen-package-regex %))

(s/def ::requirement-string
       #(re-matches str-requirement-regex %))

