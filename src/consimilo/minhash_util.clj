(ns consimilo.minhash-util
  (:require [clojure.set :refer [intersection]]))

(defn scalar-and
  "performs a scalar bitwise on each element of vec and k"
  [v k]
  (mapv (fn [e] (.and e (biginteger k))) v))

(defn scalar-mod
  "performs a scalar modulus on each element of vec and k"
  [v k]
  (mapv #(.mod % (biginteger k)) v))

(defn scalar-mul
  "performs a scalar multiply on each element of vec and k"
  [v k]
  (mapv #(.multiply % (biginteger k)) v))

(defn elementwise-add
  "performs elementwise addition betwen vectors v1 and v1"
  [v1 v2]
  (mapv #(.add %1 %2) v1 v2))

(defn elementwise-min
  "performs elementwise minimum between vectors v1 and v2"
  [v1 v2]
  (mapv #(.min %1 %2) v1 v2))

(defn jaccard
  "performs jaccard on vectors self and other"
  [self other]
  (/ (count (intersection (set self) (set other)))
     (count self)))

(defn zip-jaccard
  "returns key value pairs {minhash-key, jaccard}"
  [forest query]
  (zipmap (:top-k query)
          (map #(jaccard (:query-hash query) (get-in @forest [:keys %]))
               (:top-k query))))
