(ns consimilo.lsh-forrest
  (:require [consimilo.lsh-util :refer [get-range
                                        get-hashranges
                                        build-hashtables
                                        build-sorted-hashtables]]))

(def perms 128) ;;move to config
(def trees 8)  ;;move to config
(def k (int (/ perms trees)))
(def hashrange (get-range k trees))
(def hashranges (get-hashranges k trees))

;;TODO: change to defonce after development is complete
(def mighty-atom (atom {:keys {}
                        :hashtables (build-hashtables trees)
                        :sorted-hash (build-sorted-hashtables trees)}))

(defn- slice
  [start end coll]
  (drop start (take end coll)))

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
   :else (->> hashranges
              (map #(slice (first %) (last %) minhash))
              (plant-trees! key))))

(defn index!
  []
  (map (fn [n]
         (let [kw (keyword (str n))
               hash-keys (keys (get-in @mighty-atom [:hashtables kw]))]
               ;sorted (sort hash-keys)]
           (->> hash-keys
                (map vec)
                sort
                (swap! mighty-atom assoc-in [:sorted-hash kw]))))
    (range trees)))

(defn query
  ;;TODO: implement
  [minhash, k])


