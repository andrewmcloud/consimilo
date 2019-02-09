(ns consimilo.lsh-state
  (:require [config.core :refer [env]]
            [consimilo.lsh-util :as util]
            [consimilo.config :as config]
            [clojure.tools.logging :as log]))

;; length of minhash slices
(def k (int (/ config/perms config/trees)))

;; range of minhash
(def hashrange (util/get-range k config/trees))

;; vector of minhash slice lengths
(def hashranges (util/get-hashranges k config/trees))

(defn- populate-hastables!
  "adds each slice of the minhash to a differnt hashtable bucket"
  [forest key minhash]
  (dorun
    (map (fn [index min-slice]
           (let [kw (util/keywordize index)]
             (swap! forest assoc-in [:hashtables kw min-slice] (util/keywordize key))))
         (range config/trees)
         minhash)))

(defn- populate-keys!
  "associates a key to the list of minhash slices"
  [forest key sliced-minhashes]
  (swap! forest assoc-in [:keys (util/keywordize key)] (flatten sliced-minhashes)))

(defn plant-trees!
  "populates :hashtables and :keys with the minhash slices"
  [forest key sliced-minhashes]
  (populate-hastables! forest key sliced-minhashes)
  (populate-keys! forest key sliced-minhashes))

(defn sort-tree
  "sorts the list of keys in each of the hashtables and shoves them into a map as a value of tree-key"
  [forest tree-key]
  (->> (get-in @forest [:hashtables tree-key])
       keys
       (map vec)
       sort
       (into [])
       (assoc {} tree-key)))
