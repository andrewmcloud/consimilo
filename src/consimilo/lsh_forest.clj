(ns consimilo.lsh-forest
  (:require [consimilo.lsh-util :refer [build-sorted-hashtables
                                        build-hashtables
                                        slice-minhash
                                        keywordize
                                        tree-keys]]
            [consimilo.lsh-state :refer [plant-trees!
                                         hashranges
                                         hashrange
                                         sort-tree
                                         trees]]
            [consimilo.lsh-query :refer [query]]
            [config.core :refer [env]]))

(defn new-forest
  "Create new empty initialized forest structure."
  []
  (atom {:keys        {}
         :hashtables  (build-hashtables (:trees env))
         :sorted-hash (build-sorted-hashtables (:trees env))}))

(defn add-lsh!
  "add minhash to lsh-forest. key must be a string, will be converted to keyword"
  [forest key minhash]
  (cond
    (get-in @forest [:keys (keywordize key)]) (print "key already added to hash")
    (< (count minhash) hashrange) (print "minhash is not correct permutation size")
    :else (plant-trees! forest key (slice-minhash minhash hashranges))))

(defn index!
  "builds sorted-hash, must be called in order to query. "
  [forest]
  (swap! forest
         assoc
         :sorted-hash
         (into {} (doall (pmap (partial sort-tree forest) (tree-keys trees))))))

(defn query-forest
  "search lsh-forest for top k most similar items, utilizes binary search.
  index! must be called prior to build the sorted hashes."
  [forest minhash k-items]
  (query forest minhash k-items))
