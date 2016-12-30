(ns dependable.repository)

(defn make-nil-repo
  []
  (fn [package-name] nil))

(defprotocol IRepository
  "A protocol to enable repositories to be specified in the config file."
  (load [this] "Returns a functor taking a name and having the ability to return available packages under that name."))
  
(defrecord PriorityMultiRepo
    [repos])
(defrecord PooledMultiRepo
    [repos])
(defrecord EDNRepo
    [url])

(extend-protocol IRepository
  nil
  (load [this]
    (make-nil-repo))
  String
  (load [this]
    (make-edn-repo (URL. this))))
    
(defn make-proirity-multi-repo
  "Constructs a priority multiple repository which can be queried."
  [urls]
  (fn [package-name] nil))
(defn make-pooled-multi-repo
  "Constructs a pooled multiple repository which can be queried."
  [urls]
  (fn [package-name] nil))
(defn make-edn-repo
  "Constructs a repository from an EDN URL."
  [url]
  (fn [package-name] nil))
