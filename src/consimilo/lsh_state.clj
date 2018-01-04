(ns consimilo.lsh-state
  (:require [config.core :refer [env]]
            [consimilo.lsh-util :refer [get-hashranges
                                        keywordize
                                        get-range]]
            [clojure.tools.logging :as log]))

;; number of trees in lshforest
(def trees (if (:trees env)
             (:trees env)
             (do
               (log/warn "Number of trees cannot be configured; please ensure :trees is in the config.edn file.
                         Defaulting to 8.")
               8)))

;; length of minhash slices
(def k (if (:perms env)
         (int (/ (:perms env) trees))
         (do
           (log/warn "Number of permutations cannot be configured; please ensure :perms is in the config.edn file.
                     Defaulting to 128.")
           128)))

;; range of minhash
(def hashrange (get-range k trees))

;; vector of minhash slice lengths
(def hashranges (get-hashranges k trees))

(defn- populate-hastables!
  "adds each slice of the minhash to a differnt hashtable bucket"
  [forest key minhash]
  (dorun
    (map (fn [index min-slice]
           (let [kw (keywordize index)]
             (swap! forest assoc-in [:hashtables kw min-slice] (keywordize key))))
         (range trees)
         minhash)))

(defn- populate-keys!
  "associates a key to the list of minhash slices"
  [forest key minhash]
  (swap! forest assoc-in [:keys (keywordize key)] (flatten minhash)))

(defn plant-trees!
  "populates :hashtables and :keys with the minhash slices"
  [forest key minhash]
  (populate-hastables! forest key minhash)
  (populate-keys! forest key minhash))

(defn sort-tree
  "sorts the list of keys in each of the hashtables and shoves them into a map as a value of tree-key"
  [forest tree-key]
  (->> (get-in @forest [:hashtables tree-key])
       keys
       (map vec)
       sort
       (into [])
       (assoc {} tree-key)))
