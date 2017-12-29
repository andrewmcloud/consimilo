(ns consimilo.lsh-util
  (:require [config.core :refer [env]])
  (:import (java.util Collections)))

(defn- slice
  "Slices from start to end non incluseive."
  [start end coll]
  (drop start (take end coll)))

(defn get-hashranges
  "Vectors of [start stop] for each bucket for the given `k` buckets and `trees` trees."
  [k trees]
  (map #(vector (* % k) (* (inc %) k)) (range trees)))

(defn get-range
  "Total number of ranges for given `k` and `trees`."
  [k trees]
  (* k trees))

(defn keywordize
  "Converts integer to keyword"
  [i]
  (if (keyword? i)
    i
    (keyword (str i))))

(defn tree-keys
  "Keywords for each integer between 0 and `trees`."
  [trees]
  (mapv keywordize (range trees)))

(defn v=v
  "predicate: vector1 = vector2"
  [v1 v2]
  (zero? (compare v1 v2)))

(defn v>=v
  "predicate: vector1 >= vector2"
  [v1 v2]
  (>= (compare v1 v2) 0))

(defn build-hashtables
  "Creates map from keywords for 0 to `trees` to {}."
  [trees]
  (zipmap (map keywordize (range trees)) (repeat {})))

(defn build-sorted-hashtables
  "Creates map from keywords for 0 to `trees` to []."
  [trees]
  (zipmap (map keywordize (range trees)) (repeat [])))

(defn coll-prefix
  "returns vector of first k items in coll"
  [coll k]
  (vec (take k coll)))

(defn slice-minhash
  "Slices `minhash` at `hashranges` boundaries.
  `hashranges` is sequence of sequences each with 2 elements,
  the first is the start of the bucket range and the second
  is the end of that bucket."
  [minhash hashranges]
  (mapv #(slice (first %) (last %) minhash) hashranges))