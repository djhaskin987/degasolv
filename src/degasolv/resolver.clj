(ns degasolv.resolver
  "Namespace containing `resolve-dependencies` and supporting functions."
  (:require [degasolv.util :refer :all]
            [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [clojure.string :as clj-str]
            [flatland.ordered.set :as memset]))

(defmacro dbg [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
     (flush)
     x#))

(load "resolver_core")
(load "resolver_spec")
(load "resolver_utils")
