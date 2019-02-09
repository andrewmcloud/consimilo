(ns consimilo.lsh-forest
  (:require [consimilo.lsh-util :as util]
            [consimilo.lsh-state :as state]
            [consimilo.lsh-query :as q]
            [consimilo.config :as config]
            [clojure.tools.logging :as log]))

(defn new-forest
  "Create new empty initialized forest structure."
  []
  (atom {:keys        {}
         :hashtables  (util/build-hashtables config/trees)
         :sorted-hash (util/build-sorted-hashtables config/trees)}))

(defn add-lsh!
  "add minhash to lsh-forest. key must be a string, will be converted to keyword"
  [forest key minhash]
  (cond
    (get-in @forest [:keys (util/keywordize key)]) (log/warn "key already added to hash")
    (< (count minhash) state/hashrange) (log/warn "minhash is not correct permutation size")
    :else (state/plant-trees! forest key (util/slice-minhash minhash state/hashranges))))

(defn index!
  "builds sorted-hash, must be called in order to query. "
  [forest]
  (swap! forest
         assoc
         :sorted-hash
         (into {} (doall (pmap (partial state/sort-tree forest) (util/tree-keys config/trees))))))

(defn query-forest
  "search lsh-forest for top k most similar items, utilizes binary search.
  index! must be called prior to build the sorted hashes."
  [forest minhash k-items]
  (q/query forest minhash k-items))