(ns consimilo.util)

(defn scalar-and
  [vec, k]
  (map (fn [e] (.and e (biginteger k))) vec))

(defn scalar-mod
  [vec, k]
  (map #(.mod % (biginteger k)) vec))

(defn scalar-mul
  [vec, k]
  (map #(.multiply % (biginteger k)) vec))

(defn elementwise-add
  [v1, v2]
  (map #(.add %1 %2) v1 v2))

(defn elementwise-min
  [v1, v2]
  (map #(.min %1 %2) v1 v2))

(defn- count-equal
  [v1 v2]
  (->> (map #(.equals %1 %2) v1 v2)
       (filter true?)
       (count)))

(defn jaccard
  [self other]
  (/ (count-equal self other) (count self)))
