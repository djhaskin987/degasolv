(ns degasolv.pkgsys.debian
  "Namespace containing functions related to the debian package system."
  (:require [clojure.string :as string]))

(defmacro dbg [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
x#))

(defn-
  lexical-comparison
  "Lexically compare two characters according to debian version rules."
  [a b]
  (cond (= a b) 0
        (= a \~) -1
        (= b \~) 1
        (= a (char 0)) -1
        (= b (char 0)) 1
        (and (java.lang.Character/isLetter a)
             (not (java.lang.Character/isLetter b)))
        -1
        (and (java.lang.Character/isLetter b)
             (not (java.lang.Character/isLetter a)))
        1
        :else
        (- (int a) (int b))))

(defn- justify-strings
  "Returns two seqs of equal length, composed either
  of the characters from the strings, or the null character."
  [a b]
  (let [va (vec a)
        vb (vec b)
        ca (count a)
        cb (count b)
        nullc (char 0)]
  (cond
    (= ca
       cb)
    [va
     vb]
    (> cb ca)

    [(into va
           (repeat (- cb ca)
                   nullc))
     vb]
    :else
    [va
     (into vb
           (repeat (- ca cb)
                   nullc))])))
(defn- split
  [vers]
  (vec
    (interleave
      (string/split vers #"[0-9]+")
      (map
        #(java.lang.Integer/parseInt %)
        (string/split vers #"[^0-9]+")))))

(defprotocol ^:private Default
  (default [this]))

(extend-protocol Default
  clojure.lang.BigInt
  (default [this] 0N)
  java.math.BigDecimal
  (default [this] 0M)
  java.lang.Long
  (default [this] 0)
  java.lang.Integer
  (default [this] 0)
  java.lang.Double
  (default [this] 0.0)
  java.lang.String
  (default [this] ""))

(defn- justify
  [a b]
  (cond
    (= (count a)
       (count b))
    [a b]
    (> (count b) (count a))
    [(into
       a
       (map default (subvec b (count a))))
     b]
    :else
    [a
     (into
       b
       (map default (subvec a (count b))))]))

(defprotocol ^:private DebianPartCompare
  (part-cmp [a b]))

(extend-protocol DebianPartCompare
  java.lang.Integer
  (part-cmp
    [a b]
    (- a b))
  java.lang.Long
  (part-cmp
    [a b]
    (- a b))
  java.lang.String
  (part-cmp
    [a b]
    (or
      (some
        #(if (not (zero? %)) % nil)
        (let [[justa justb] (justify-strings a b)]
          (map
            lexical-comparison
            justa
            justb)))
      0)))

(defn vercmp
  [a b]
  (or
    (some
      #(if (not (zero? %)) % nil)
      (let [[justa justb] (justify (split a) (split b))]
        (map
          part-cmp
          justa
          justb)))
    0))

(defn deb-to-degasolv-requirement
  [s]
  (string/split (string/replace (string/replace (string/replace (string/replace s #"[ ()]" "") #"<<" "<")#">>" ">") "," " ") #" "))

(defn restructure-apt-output
  [output]
  (map
    (fn foreach-thing [x]
      (into
        {}
        (map
          (fn foreach-string [y]
            (string/split y #": ")) x)))
    (map #(apply concat %)
         (partition
           2
           (partition-by
             #(re-matches #"^Package:.*" %)
             (filter
               #(re-matches #"^(Package|Depends|Filename|Origin):.*" %)
               (string/split-lines
                 output)))))))
