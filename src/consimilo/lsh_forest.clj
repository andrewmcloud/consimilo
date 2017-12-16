(ns consimilo.lsh-forest
  (:require [consimilo.lsh-util :refer [get-range
                                        get-hashranges
                                        build-hashtables
                                        build-sorted-hashtables
                                        slice-minhash
                                        coll-prefix
                                        pred-search
                                        tree-keys
                                        v>=v
                                        v=v]]))


(def perms 128)                                             ;;TODO move to config
(def trees 8)                                               ;;TODO move to config
(def k (int (/ perms trees)))
(def hashrange (get-range k trees))
(def hashranges (get-hashranges k trees))

;;maintains state of lshforest
(def mighty-atom (atom {:keys        {}
                        :hashtables  (build-hashtables trees)
                        :sorted-hash (build-sorted-hashtables trees)}))

(defn- populate-hastables!
  "adds each slice of the minhash to a differnt hashtable bucket"
  [key bt-arrays]
  (dorun
    (map (fn [index bt-array]
           (let [kw (keyword (str index))]
             (swap! mighty-atom assoc-in [:hashtables kw bt-array] key)))
         (range trees)
         bt-arrays)))

(defn- populate-keys!
  "associates a key to the list of minhash slices"
  [key bt-arrays]
  (swap! mighty-atom assoc-in [:keys (keyword key)] (flatten bt-arrays)))

(defn- plant-trees!
  "populates :hashtables and :keys with the minhash slices"
  [key byte-arrays]
  (populate-hastables! key byte-arrays)
  (populate-keys! key byte-arrays))

(defn addlsh!
  [key, minhash]
  (cond
    (get-in @mighty-atom [:keys (keyword key)]) (print "key already added to hash")
    (< (count minhash) hashrange) (print "minhash is not correct permutation size")
    :else (plant-trees! key (slice-minhash minhash hashranges))))

(defn sort-tree
  [mighty coll tree]
  (let [kw (keyword (str tree))]
    (->> (get-in mighty [:hashtables kw])
         keys
         (map vec)
         sort
         (assoc coll kw))))

(defn index!
  []
  (swap! mighty-atom
         assoc
         :sorted-hash
         (doall (reduce (partial sort-tree @mighty-atom) {} (range trees)))))

(defn- hashtable-lookup
  [key]
  (map #(get-in @mighty-atom [:hashtables % key]) (tree-keys trees)))

(defn hashtables-lookup
  [keys]
  (map hashtable-lookup keys))

(defn- query-fn
  [min-slice, tk, r]
  (let [sorted (get-in @mighty-atom [:sorted-hash tk])
        hashtable (get-in @mighty-atom [:hashtables tk])
        min-prefix (coll-prefix min-slice r)
        sorted-range (dec (count sorted))
        i (pred-search (fn [x]
                         (v>=v
                           (coll-prefix (get sorted x) r)
                           min-prefix))
                       sorted-range)]

    (if (v=v (coll-prefix (get sorted i) r) min-prefix)
      (vec (take-while #(v=v (coll-prefix (get sorted %) r) min-prefix) (drop i sorted))))))

(defn- query-k-prefix
  [minhash r]
  (mapcat #(query-fn %1 %2 r) (slice-minhash minhash hashranges)
                              (tree-keys trees)))

(defn query
  [minhash, k-items]
  (if (<= k-items 0)
    (print "k must be greater than zero"))
  (if (< (count minhash) (* k trees))
    (print ("the numperm of Minhash out of range")))
  (->> (take k-items (mapcat #(query-k-prefix minhash %) (reverse (range k))))
       hashtables-lookup
       flatten
       (filterv some?)))


;TODO lookup items in mighty-atom :keys
