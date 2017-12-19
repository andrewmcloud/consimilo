(ns consimilo.lsh-forest
  (:require [consimilo.lsh-util :refer [slice-minhash tree-keys]]
            [consimilo.lsh-state :refer [mighty-atom plant-trees! sort-tree hashranges hashrange trees]]
            [consimilo.lsh-query :refer [query]]))

(defn add-lsh!
  "add minhash to lsh-forest. key must be a string, will be converted to keyword"
  [key, minhash]
  (cond
    (get-in @mighty-atom [:keys (keyword key)]) (print "key already added to hash")
    (< (count minhash) hashrange) (print "minhash is not correct permutation size")
    :else (plant-trees! key (slice-minhash minhash hashranges))))

(defn index!
  "builds sorted-hash, must be called in order to query. "
  []
  (swap! mighty-atom
         assoc
         :sorted-hash
         (into {} (doall (pmap sort-tree (tree-keys trees))))))

(defn query-forest
  "search lsh-forest for top k most similar items, utilizes binary search.
  index! must be called prior to build the sorted hashes."
  [minhash k-items]
  (query minhash k-items))
