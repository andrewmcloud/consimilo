(ns consimilo.minhash-util)

(defn scalar-and
  "performs a scalar bitwise on each element of vec and k"
  [v k]
  (map (fn [e] (.and e (biginteger k))) v))

(defn scalar-mod
  "performs a scalar modulus on each element of vec and k"
  [v k]
  (map #(.mod % (biginteger k)) v))

(defn scalar-mul
  "performs a scalar multiply on each element of vec and k"
  [v k]
  (map #(.multiply % (biginteger k)) v))

(defn elementwise-add
  "performs elementwise addition betwen vectors v1 and v1"
  [v1 v2]
  (map #(.add %1 %2) v1 v2))

(defn elementwise-min
  "performs elementwise minimum between vectors v1 and v2"
  [v1 v2]
  (map #(.min %1 %2) v1 v2))

(defn- intersection-ct
  "counts the intersection between vectors v1 and v2"
  [v1 v2]
  (->> (map #(.equals %1 %2) v1 v2)
       (filter true?)
       count))

(defn jaccard
  "performs jaccard on vectors self and other"
  [self other]
  (/ (intersection-ct self other) (count self)))
