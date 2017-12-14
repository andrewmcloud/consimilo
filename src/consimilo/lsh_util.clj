(ns consimilo.lsh-util
  (:import (java.util Collections)))

(defn get-hashranges
  "Vectors of [start stop] for each bucket for the given `k` buckets and `trees` trees."
  [k trees]
  (map #(vector (* % k) (* (inc %) k)) (range trees)))

;;returns number of buckets
(defn get-range
  "Total number of ranges for given `k` and `trees`."
  [k trees]
  (* k trees))

(defn keyword-int
  "Converts integer to keyword"
  [i]
  (keyword (str i)))

;;builds hashtables data structure - {1: {} 2: {} ... trees: {}}
(defn build-hashtables
  "Creates map from keywords for 0 to `trees` to {}."
  [trees]
  (zipmap (map keyword-int (range trees)) (repeat {})))

;;builds sorted-hashtables data structure - {1: [] 2: [] ... trees: []}
(defn build-sorted-hashtables
  "Creates map from keywords for 0 to `trees` to []."
  [trees]
  (zipmap (map keyword-int (range trees)) (repeat [])))

(defn- slice
  "Slices from start to end non incluseive."
  [start end coll]
  (drop start (take end coll)))

(defn slice-minhash
  "Slices `minhash` at `hashranges` boundaries.
  `hashranges` is sequence of sequences each with 2 elements,
  the first is the start of the bucket range and the second
  is the end of that bucket."
  [minhash hashranges]
  (map #(slice (first %) (last %) minhash) hashranges))

(defn tree-keys
  "Keywords for each integer between 0 and `trees`."
  [trees]
  (mapv keyword-int (range trees)))

(defn pred-search
  "Finds the first index less then `j` for which `pred` is satisfied."
  ([pred j]
   (pred-search pred j 0))
  ([pred j i]
   (println i j)
   (if (>= i j)
     i
     (let [h (int (+ i (/ (- j i)) 2))]
       (println h)
       (if-not (pred h)
         (recur pred j (inc h))
         (recur pred h i))))))