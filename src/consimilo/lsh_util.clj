(ns consimilo.lsh-util
  (:import (java.util Collections)))

(defn get-hashranges
  "Vectors of [start stop] for each bucket for the given `k` buckets and `trees` trees."
  [k trees]
  (map #(vector (* % k) (* (inc %) k)) (range trees)))

(defn get-range
  "Total number of ranges for given `k` and `trees`."
  [k trees]
  (* k trees))

(defn keyword-int
  "Converts integer to keyword"
  [i]
  (keyword (str i)))

(defn v=v
  [v1 v2]
  (= (compare v1 v2) 0))

(defn v>=v
  [v1 v2]
  (>= (compare v1 v2) 0))

(defn build-hashtables
  "Creates map from keywords for 0 to `trees` to {}."
  [trees]
  (zipmap (map keyword-int (range trees)) (repeat {})))

(defn build-sorted-hashtables
  "Creates map from keywords for 0 to `trees` to []."
  [trees]
  (zipmap (map keyword-int (range trees)) (repeat [])))

(defn- slice
  "Slices from start to end non incluseive."
  [start end coll]
  (drop start (take end coll)))

(defn coll-prefix
  [coll k]
  (vec (slice 0 k coll)))

(defn slice-minhash
  "Slices `minhash` at `hashranges` boundaries.
  `hashranges` is sequence of sequences each with 2 elements,
  the first is the start of the bucket range and the second
  is the end of that bucket."
  [minhash hashranges]
  (mapv #(slice (first %) (last %) minhash) hashranges))

(defn tree-keys
  "Keywords for each integer between 0 and `trees`."
  [trees]
  (mapv keyword-int (range trees)))

(defn pred-search
  "Finds the first index less then `j` for which `pred` is satisfied."
  ([pred j]
   (pred-search pred j 0))
  ([pred j i]
   (if (>= i j)
     i
     (let [h (int (+ i (/ (- j i) 2)))]
       (if-not (pred h)
         (recur pred j (inc h))
         (recur pred h i))))))