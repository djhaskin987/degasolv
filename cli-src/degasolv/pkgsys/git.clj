(ns degasolv.pkgsys.git
  "Namespace containing functions related to the git package system
   integration point."
  (:require [clojure.string :as string]
            [clojure.java.shell :as sh]
            [cheshire.core :as json]
            [clojure.walk :as walk]
            [miner.tagged :as tag]
            [degasolv.util :refer :all]
            [degasolv.resolver :as r :refer :all]))

(defn make-query [options]
  )
