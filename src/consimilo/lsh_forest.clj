(ns consimilo.lsh-forest
  (:require [consimilo.lsh-util :refer [get-range
                                        get-hashranges
                                        build-hashtables
                                        build-sorted-hashtables
                                        slice-minhash
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
         (reduce (partial sort-tree @mighty-atom) {} (range trees))))

(defn- query-fn
  [min-slice, tk]
  (let [sorted (get-in @mighty-atom [:sorted-hash tk])
        hashtable (get-in @mighty-atom [:hashtables tk])
        i (pred-search (count sorted) (fn [x] (v>=v (get sorted x) min-slice)))]
    (if (and (< i (count sorted)) (v=v (get sorted i) min-slice))
      (take-while #(= (get sorted %) min-slice) (drop i sorted)))))

(defn _query
  [minhash r]
  (mapcat query-fn (slice-minhash minhash hashranges)
                   (tree-keys trees)))

(defn query
  [minhash, k-items]
  (if (<= k-items 0)
    (print "k must be greater than zero"))
  (if (< (count minhash) (* k trees))
    (print ("the numperm of Minhash out of range")))
  (take k-items (mapcat #(_query minhash %) (reverse (range k)))))
