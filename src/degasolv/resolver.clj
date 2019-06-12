(ns degasolv.resolver
  "Namespace containing `resolve-dependencies` and supporting functions."
  (:require [degasolv.util :refer :all]
            [clojure.spec.alpha :as s]
            [clojure.string :as clj-str]
            [miner.tagged :as tag]))

(defmacro dbg [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
     x#))

(load "resolver_core")
(load "resolver_spec")
(load "resolver_utils")
