(ns degasolv.resolver
  "Namespace containing `resolve-dependencies` and supporting functions."
  (:require [degasolv.util :refer :all]
            [clojure.set :as set]
            [clojure.spec.alpha :as s]
            [clojure.string :as clj-str]
            [miner.tagged :as tag]
            [flatland.ordered.set :as memset]))

(defmacro dbg [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
     x#))

(load "resolver_core")
(load "resolver_spec")
(load "resolver_utils")
