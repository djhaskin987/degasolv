(ns degasolv.util
  (:require [clojure.java.io :as io]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clj-http.lite.client :as client]))

; UTF-8 by default :)
(defn base-slurp [loc]
  (let [input (if (= loc "-")
                    *in*
                    loc)]
    (clojure.core/slurp input :encoding "UTF-8")))

(defn default-slurp [resource]
  (if (re-matches #"https?://.*" (str resource))
              (:body
      (if-let [[whole-thing protocol auth-stuff rest-of-it]
               (re-matches #"(https?://)([^@]+)@(.+)" resource)]
        (if-let [[_ username password]
                 (re-matches #"([^:]+):([^:]+)" auth-stuff)]
          (client/get (str
                        protocol
                        rest-of-it)
                      {
                       :basic-auth [(java.net.URLDecoder/decode username)
                                    (java.net.URLDecoder/decode password)]})
          (if-let [[_ headerkey headerval]
                   (re-matches #"([^=]+)=([^=]+)" auth-stuff)]
            (client/get (str
                               protocol
                               rest-of-it)
                        {
                              :headers
                              {
                               (keyword (java.net.URLDecoder/decode headerkey))
                               (java.net.URLDecoder/decode headerval)}})
            (client/get (str
                          protocol
                          rest-of-it)
                        {
                         :oauth-token (java.net.URLDecoder/decode auth-stuff)
                         })))
        (client/get resource)))
                  (base-slurp resource)))


(defn default-spit [loc stuff]
  (clojure.core/spit loc (pr-str stuff) :encoding "UTF-8"))

(defn pretty-spit [loc stuff]
  (with-open
    [ow (io/writer loc :encoding "UTF-8")]
    (pprint/pprint stuff ow)))
