(ns consimilo.minhash
  (:require [consimilo.random-seed :refer [set-random-seed! rand-vec]]
            [consimilo.sha1 :refer [get-hash-int]]
            [consimilo.util :refer [scalar-and
                                    scalar-mod
                                    scalar-mul
                                    elementwise-add
                                    elementwise-min]]
            [clojure.core :exclude [rand-int]]))

(def mersenne (biginteger (- (bit-shift-left 1 61) 1)))
(def max-hash (biginteger (- (bit-shift-left 1 32) 1)))
(def seed 1)
(def perms 4)


(defn- init-hashvalues
  []
  (->> (vec (replicate perms (biginteger 1)))
       (map #(.multiply mersenne %))))

(defn- build-permutations
  []
  (set-random-seed! seed)
  (-> (assoc {} :a (rand-vec perms mersenne))
      (assoc :b (rand-vec perms mersenne))))

(defn update-minhash
  [hashvalues bt]
  (print "update-minhash")
  (let [permutations (build-permutations)
        hv (get-hash-int bt)
        a (:a permutations)
        b (:b permutations)]
    (-> (scalar-mul a hv)
        (elementwise-add b)
        (scalar-mod mersenne)
        (scalar-and max-hash)
        (elementwise-min hashvalues))))

(defn build-minhash
  ([bt-vec]
   (build-minhash bt-vec (init-hashvalues)))

  ([[bt & rest] hashvalues]
   (print hashvalues)
   (if (nil? bt)
     hashvalues
     (recur (update-minhash hashvalues bt) rest))))

(defn test-minhash
  []
  (build-minhash ["a" "b" "d"]))
