(ns degasolv.resolver
  "Namespace containing `resolve-dependencies` and supporting functions."
  (:require [degasolv.util :refer :all]
            [clojure.string :as clj-str]))


(load "resolver_utils")
(load "resolver_core")

#_(defmacro dbg [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
x#))
