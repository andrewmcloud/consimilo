(ns consimilo.lsh-state
  (:require [config.core :refer [env]]
            [consimilo.lsh-util :refer [get-hashranges get-range keyword-int]]))

(def trees (:trees env))                         ;; number of trees in lshforest
(def k (int (/ (:perms env) trees)))             ;; length of minhash slices
(def hashrange (get-range k trees))              ;; range of minhash
(def hashranges (get-hashranges k trees))        ;; vector of minhash slice lengths

(defn- populate-hastables!
  "adds each slice of the minhash to a differnt hashtable bucket"
  [forest key bt-arrays]
  (dorun
    (map (fn [index bt-array]
           (let [kw (keyword-int index)]
             (swap! forest assoc-in [:hashtables kw bt-array] key)))
         (range (:trees env))
         bt-arrays)))

(defn- populate-keys!
  "associates a key to the list of minhash slices"
  [forest key bt-arrays]
  (swap! forest assoc-in [:keys (keyword key)] (flatten bt-arrays)))

(defn plant-trees!
  "populates :hashtables and :keys with the minhash slices"
  [forest key byte-arrays]
  (populate-hastables! forest key byte-arrays)
  (populate-keys! forest key byte-arrays))

(defn sort-tree
  "sorts the list of keys in each of the hashtables and shoves them into a map as a value of tree-key"
  [forest tree-key]
  (->> (get-in @forest [:hashtables tree-key])
       keys
       (map vec)
       sort
       (into [])
       (assoc {} tree-key)))
