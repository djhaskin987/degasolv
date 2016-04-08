(ns dependable.resolve
  (:require [schema.core :as s
             clojure.core.match :as m]))

(s/defrecord 
(s/defrecord StampedNames
  [date :- Long
   names :- [s/Str]])

(s/defn stamped-names :- StampedNames
  [names :- [s/Str]]
  (StampedNames. (str (System/currentTimeMillis)) names))

(defrecord StampedNames
  [^Long date
   names ;; a list of Strings
   ])

(defn ^StampedNames stamped-names
  "names is a list of Strings"
  [names]
  (StampedNames. (str (System/currentTimeMillis)) names))
(def



(defprotocol spec
  (resolve-dep [

(defprotocol spec
  (resolve-dep []))

; type: spec. spec is a map from names to predicates
; NameSpec name
; VersionSpec Name Predicate
; DisjunctionSpec(listof Spec)
; ConjunctionSpec(listof Spec)
; spec = NameSpec | VersionSpec | DisjunctionSpec | ConjunctionSpec
; resolveDeps spec =>
; NameSpec -> insert (realize name) into graph returned by (resolve (deps (realize name))), return that
; VersionSpec -> insert (Realize name spec query) into graph returned by "" "" "" blah blah blah
; DisjunctionSpec -> Resolve first. If that works, return it. Else ...
; ConjunctionSpec -> Resolve all, then reduce them using Reconcile into one graph. return that graph.
;
; PackageGraph PackageRecords ParentRecords
; PackageRecords: (name -> package, name -> name)
; ParentRecords: (name -> name)
;
; Then we have a function, project, which takes a package graph and returns a list of all the packages.
; Also, a function resolve-dependencies, which is simply (project (resolve-dep <spec>))
; for EDN purposes, model above types as one-element structs
(defrecord name-spec [name])
(defrecord version-spec [name predicate])
(defrecord


; records: name -> package
; dependee-parents: name -> name
; conflicts: name -> spec
; dependencies: name -> spec
(defrecord package-graph [records dependee-parents])
(defrecord package [name version location conflicts dependencies])




(defn pred-call [f v]
  (f v))

(def safe-pred-call
  (fnil spec-call (fn [v] true)))



; helper functions
(defn merge-graphs [a b]
  (->package-graph (into (:records a) (:records b))
                   (into (:dependee-parents a) (:dependee-parents b))))

(defn conflicts
  "Encapsulates the idea behind two graphs conflicting.
  Returns a pair, where the first element is a list of package names
  conflicting in a, and the second element is a list of package names
  conflicting in graph b.  This examines the conflicts field in the packages of
  the graphs, but also this func returns non-nil if packages in the graphs
  share the same name but are of different versions, etc.
  This is a problem because two packages sharing the same name but of different
  versions -- this case of two graphs conflicting are easy to check, O(n log
  n).  However, checking the `conflicts` field of one package against all the
  names of the others is an O(n^2) operation. So it's cubic time to reduce
  stuff. Maybe I *should* reduce the graphs in parallel.
  However, it's not cubic time in the average case. It is expected that the
  'conflicts' field will be sparse; that is, it will be used infrequently
  and not many packages will be in the 'conflicts' list. This can be used
  to save the average case and reduce the problem down to an O(n) average
  case operation."
  [a b])


(defn remove-dep
  [gph nm])

(defn reconcile
  "Reconcile two graphs.
  Find any conflicts. If none, merge. If conflicts,
  1) remove the conflicting packages
  2) create an independent graph using the specs that were fulfilled by the
     removed packages (what was left hanging) together with the additional
     standing conflicts of `don't use these packages at these versions` (the
     ones which were in conflict).
  3) reconcile the graphs with removed things and the new graph recursively.
  It's hard to figure out syncs and sources, and I don't need to."
  [a b query])

#_(defn realize
  "Takes a request object and a query object and
  returns an actual package object pull from the
  query object, which satisfies the request"
  [query request]
  (first
    (filter
      #(safe-spec-call (:version-spec request) (:version %))
      (query (:name request)))))

(defn realize
  [nm spec query])

; -> package-graph
(defn resolve-dep
  [spec])

(defn resolve-deps
  [specs])





; tested functions
; graph name -> graph
; takes graph and removes named package from it and,
; recursively, any package present in the graph which fulfills
; a dependency of that package, and only that package.
#_(defn remove-dep
  [to from node]
  (let [children (:children (to node))
        children-names (keys children)
        from' (reduce
                (fn [c [child _parents]]
                  (if (= (count _parents) 1)
                    (dissoc c child)
                    (assoc c
                           child
                           (disj _parents node))))
                from
                (map #(find from %)
                     children-names))
        to' (dissoc to
                    node)]
    (reduce (fn [c [nm version-spec]]
              (let [[to'' from''] c]
                (if (contains? from' nm)
                  c
                  (remove-dep to'' from'' nm))))
            [to' from']
            children)))


(defn resolve-deps [package]
  #_(reduce
    reconcile
    (->package-graph {(:name package) package} {})
    (map #(resolve-deps
            (realize %1))
         (:dependencies package))))


; package-graph name -> {name: listof spec}
; Return a map from names to list of package specs that will
; be "dangling" specifications if "ds" were removed.
; ex. make a map from names to dangling specs like so:
; (into {} (map vector names (dangling names)))
#_(defn dangling [gph ds]
  (into {}
        (map vector d
             (let [d-parents (d (:dependee-parents gph))]
               (map (fn [handle]
                      (d (handle (:records gph)))) d-parents)))))


#_(defn remove-all-deps [gph d-names]
  (reduce #(remove-deps %1 %2)
                     gph
                     d-names))

; realize is already written
; needs work to be able to handle conflicts, already-installeds, etc.
; Therefore: ONLY SOLVE THE REQUIRES / DIAMOND TESTS FIRST
#_(defn reconcile [a b]
  #_(let [cs (conflicts a b)]
      (if (empty? cs)
        (merge-graphs a b)
        (let [a-dangl (dangling a cs)
              a' (remove-all-deps a cs)
              b-dangl (dangling b cs)
              b' (remove-all-deps b cs)
              dangl (reduce-kv (fn [c k v]
                                 (if (contains? c k)
                                   (assoc c k (into (c k) v))
                                   (assoc c k v)))
                               a-dangl
                               b-dangl)
              tried (graph-resolve a' dangl query)]



    )

#_(defn resolve-deps
  [specs
   query &
   {:keys [already-found
           conflicts]
    :or {already-found {}
         conflicts {}}}]
  (loop [remaining (seq specs)
         installed already-found
         conflict conflicts
         result #{}]
    (if (empty? remaining)
      [:successful result]
      (let [pkg (first remaining)
            r (rest remaining)
            pname (:name pkg)
            pspec (:version-spec pkg)]
        (cond (contains? installed pname)
              (if (not (safe-spec-call pspec (installed pname)))
                [:unsatisfiable [pname]]
                (recur r installed conflict result))
              (and (contains? conflict pname)
                   (nil? (conflict pname)))
              [:unsatisfiable [pname]]
              :else
              (let [chosen (choose-candidate pname pspec query)
                    chosen-conflicts (:conflicts chosen)]
                (if (or (nil? chosen)
                        (and (contains? conflict pname)
                             (safe-spec-call
                               (conflict pname)
                               (:version chosen))))
                  [:unsatisfiable [pname]]
                  (recur r (assoc
                             installed
                             (:name chosen)
                             (:version chosen))
                         (into
                           conflict
                           chosen-conflicts)
                         (conj result chosen)))))))))
