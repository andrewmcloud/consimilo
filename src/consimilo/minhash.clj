(ns consimilo.minhash
  (:require [clojure.core.matrix :as m]
            [consimilo.random-seed :refer [set-random-seed! rand-int]]
            [consimilo.sha1 :refer [get-hash-int]]
            [consimilo.util :refer [elementwise-and elementwise-mod]]
            [clojure.core :exclude [rand-int]]))

(m/set-current-implementation :vectorz)

(def mersenne (biginteger (- (bit-shift-left 1 61) 1)))
(def max-hash (biginteger (- (bit-shift-left 1 32) 1)))
(def hash-range (biginteger (bit-shift-left 1 32)))
(def seed 1)
(def perms 16)


(defn- init_hashvalues
  []
  (-> (m/new-vector perms)
      (m/fill 1)
      (m/mul max-hash)))

(defn- build-permutations
  [num-perm]
  (let [shape [2, num-perm]]
    (set-random-seed! seed)
    (m/compute-matrix shape (fn [_ _] (rand-int mersenne)))))

(defn update-minhash
  [hashvalues bt]
  (let [permutations (build-permutations perms)
        hv (get-hash-int bt)
        a (m/get-row permutations 0)
        b (m/get-row permutations 1)
        phv (elementwise-and (elementwise-mod (m/add (m/emul! a hv) b) mersenne) max-hash)]
    (m/emap! (fn [a b] (min a b)) phv hashvalues)))

(defn build-minhash
  ([byte-vec]
   (build-minhash (init_hashvalues) byte-vec))

  ([hashvalues [bytes &rest]]
   (if (nil? bytes)
     hashvalues
     (recur (update-minhash hashvalues bytes) rest))))


(defn test-minhash
  []
  (build-minhash ["andrew" "christina" "david"]))
