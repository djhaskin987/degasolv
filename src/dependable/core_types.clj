(defrecord requirement [status id spec])

(defn present
  ([id] (present id nil))
  ([id spec] (->requirement :present id spec)))

(defn absent
  ([id] (absent id nil))
  ([id spec] (->requirement :absent id spec)))

(defrecord package [id version location requirements])
