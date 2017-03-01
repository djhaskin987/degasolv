(ns degasolv.resolver
  "Namespace containing `resolve-dependencies` and supporting functions."
  (:require [degasolv.util :refer :all]
            [clojure.spec :as s]
            [clojure.string :as clj-str]))


(load "resolver_core")
(load "resolver_spec")
(load "resolver_utils")

#_(defmacro dbg [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
x#))
