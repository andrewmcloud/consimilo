(ns consimilo.lsh-util)

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