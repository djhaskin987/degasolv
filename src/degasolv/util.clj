(ns degasolv.util
  (:require [clojure.java.io :as io]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clj-http.client :as client]))

; Deprecated, we should just use update-in
(defn assoc-conj
  [mp k v]
  (update-in mp [k] conj v))

; UTF-8 by default :)
(defn base-slurp [loc]
  (let [input (if (= loc "-")
                    *in*
                    loc)]
    (clojure.core/slurp input :encoding "UTF-8")))

(defn default-slurp [resource]
  (if-let [[whole-thing protocol auth-stuff rest-of-it]
           (re-matches #"(https?://)([^@]+)@(.+)" resource)]
    (if-let [[_ username password]
             (re-matches #"([^:]+):([^:]+)" auth-stuff)]
      (client/get (str
                    protocol
                    rest-of-it)
                  {:basic-auth [(java.net.URLDecoder/decode username)
                                (java.net.URLDecoder/decode password)]})
      (client/get (str
                    protocol
                    rest-of-it)
                  {:headers
                   {"Authorization" (str "Bearer "
                                         (java.net.URLDecoder/decode
                                           auth-stuff))}}))
    (base-slurp resource)))


(defn default-spit [loc stuff]
  (clojure.core/spit loc (pr-str stuff) :encoding "UTF-8"))

(defn pretty-spit [loc stuff]
  (with-open
    [ow (io/writer loc :encoding "UTF-8")]
    (pprint/pprint stuff ow)))
