(ns consimilo.lsh-query
  (:require [consimilo.lsh-state :as state]
            [consimilo.lsh-util :as util]
            [consimilo.config :as config]
            [clojure.tools.logging :as log]))

(defn- hashtable-lookup
  "returns collection of values for key in nested hashtable {:tree {:key value}....}"
  [hashtable key]
  (map #(get-in hashtable [% key]) (util/tree-keys config/trees)))

(defn- hashtables-lookup
  "returns collection of values for keys in nested hashtable"
  [hashtable keys]
  (map #(hashtable-lookup hashtable %) keys))

(defn- pred-search
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

(defn- query-fn
  "performs a binary search to find the r-length prefix over the sorted hashtables"
  [forest min-slice tk r]
  (let [sorted (get-in @forest [:sorted-hash tk])
        hashtable (get-in @forest [:hashtables tk])
        min-prefix (util/coll-prefix min-slice r)
        sorted-range (dec (count sorted))
        i (pred-search (fn [x]
                         (util/v>=v
                           (util/coll-prefix (get sorted x) r)
                           min-prefix))
                       sorted-range)]
    (if (util/v=v (util/coll-prefix (get sorted i) r) min-prefix)
      (take-while #(util/v=v (util/coll-prefix % r) min-prefix) (drop i sorted)))))

(defn- query-k-prefix
  "queries for the r-length prefix of each minhash slice in the forest"
  [forest minhash r]
  (mapcat #(query-fn forest %1 %2 r)
          (util/slice-minhash minhash state/hashranges)
          (util/tree-keys config/trees)))

(defn query
  "returns a list of the keys of the top k-items most similar to minhash"
  [forest k-items minhash]
  (cond
    (<= k-items 0) (log/warn "k must be greater than zero")
    (< (count minhash) (* state/k config/trees)) (log/warn "the perm of Minhash out of range")
    :else (->> (range state/k)
               reverse
               (mapcat #(query-k-prefix forest minhash %))
               (hashtables-lookup (get @forest :hashtables))
               flatten
               (filter some?)
               (distinct)
               (take k-items))))