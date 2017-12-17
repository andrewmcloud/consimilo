(ns consimilo.lsh-state
  (:require [config.core :refer [env]]
            [consimilo.lsh-util :refer [build-hashtables build-sorted-hashtables get-hashranges get-range]]))

;;maintains state of lshforest
(defonce mighty-atom (atom {:keys        {}
                            :hashtables  (build-hashtables (:trees env))
                            :sorted-hash (build-sorted-hashtables (:trees env))}))

(def trees (:trees env))                         ;; number of trees in lshforest
(def k (int (/ (:perms env) trees)))             ;; length of minhash slices
(def hashrange (get-range k trees))              ;; range of minhash
(def hashranges (get-hashranges k trees))        ;; vector of minhash slice lengths

(defn- populate-hastables!
  "adds each slice of the minhash to a differnt hashtable bucket"
  [key bt-arrays]
  (dorun
    (map (fn [index bt-array]
           (let [kw (keyword (str index))]
             (swap! mighty-atom assoc-in [:hashtables kw bt-array] key)))
         (range (:trees env))
         bt-arrays)))

(defn- populate-keys!
  "associates a key to the list of minhash slices"
  [key bt-arrays]
  (swap! mighty-atom assoc-in [:keys (keyword key)] (flatten bt-arrays)))

(defn plant-trees!
  "populates :hashtables and :keys with the minhash slices"
  [key byte-arrays]
  (populate-hastables! key byte-arrays)
  (populate-keys! key byte-arrays))

(defn sort-tree
  "sorts the list of keys in each of the hashtables and shoves them into a map as a value of tree-key"
  [tree-key]
  (->> (get-in @mighty-atom [:hashtables tree-key])
       keys
       (map vec)
       sort
       (into [])
       (assoc {} tree-key)))
