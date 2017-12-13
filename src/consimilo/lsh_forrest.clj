(ns consimilo.lsh-forrest
  (:require [consimilo.lsh-util :refer [get-range
                                        get-hashranges
                                        build-hashtables
                                        build-sorted-hashtables
                                        slice-minhash
                                        func-search]]))


(def perms 128) ;;move to config
(def trees 8)  ;;move to config
(def k (int (/ perms trees)))
(def hashrange (get-range k trees))
(def hashranges (get-hashranges k trees))

;;TODO: change to defonce after development is complete
(def mighty-atom (atom {:keys {}
                        :hashtables (build-hashtables trees)
                        :sorted-hash (build-sorted-hashtables trees)}))

(defn- populate-hastables!
  [key bt-arrays]
  (doall
    (map (fn [index bt-array]
           (let [kw (keyword (str index))]
             (swap! mighty-atom assoc-in [:hashtables kw bt-array] key)))
      (range trees)
      bt-arrays)))

(defn- populate-keys!
  [key bt-arrays]
  (swap! mighty-atom assoc-in [:keys (keyword key)] bt-arrays))

(defn- plant-trees!
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
  [coll tree]
  (let [kw (keyword (str tree))]
    (->> (get-in @mighty-atom [:hashtables kw])
         keys
         (map vec)
         sort
         (assoc coll kw))))

(defn index!
  []
  (->>(reduce sort-tree {} (range trees))
      (swap! mighty-atom assoc-in [:sorted-hash])))

(defn- query-fn
  [hashtable, sorted, min-slice]
  (let [i (func-search (count sorted) (fn [x] (get sorted x) >= min-slice))]
    (if (and (< i (count hashtable)) (= (get sorted i) min-slice))
      (loop))))


(defn- _query
  [minhash r]
  (map query-fn (:hashtables @mighty-atom)
                (:sorted-hash @mighty-atom)
                (slice-minhash minhash hashranges)))



(defn query
  ;;TODO: implement
  [minhash, k]
  (if (<= 0 k)
    (print "k must be greater than zero"))
  (if (< (count minhash) (* k trees))
    (print ("the numperm of Minhash out of range")))
  (set (map #(_query minhash %) (range k))))



