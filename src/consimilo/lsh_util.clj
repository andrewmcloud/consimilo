(ns consimilo.lsh-util
  (:import (java.util Collections)))

;;buckets
(defn get-hashranges
  [k trees]
  (map (fn [i]
         [(* i k) (* (+ i 1) k)])
       (range trees)))

;;returns number of buckets
(defn get-range
  [k trees]
  (* k trees))

;;builds hashtables data structure - {1: {} 2: {} ... trees: {}}
(defn build-hashtables
  [trees]
  (->> (range trees)
       (map #(hash-map (keyword (str %)) {}))
       (into {})))

;;builds sorted-hashtables data structure - {1: [] 2: [] ... trees: []}
(defn build-sorted-hashtables
  [trees]
  (into {}
        (map #(hash-map (keyword (str %)) []) (range trees))))

(defn- slice
  [start end coll]
  (drop start (take end coll)))

(defn slice-minhash
  [minhash hashranges]
  (map #(slice (first %) (last %) minhash) hashranges))

(defn java-binsearch
  [xs x]
  (Collections/binarySearch xs x compare))

(defn func-search
  ([func j]
   (func-search func j 0))

  ([func j i]
   (if (> i j)
     i
     (let [h (int (/ (+ i (- j i)) 2))]
       (if (not (func h))
         (recur func j (+ h 1))
         (recur func h i))))))