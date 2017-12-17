(ns consimilo.minhash
  (:require [consimilo.random-seed :refer [set-random-seed! rand-vec]]
            [consimilo.sha1 :refer [get-hash-biginteger]]
            [config.core :refer [env]]
            [consimilo.minhash-util :refer [scalar-and
                                            scalar-mod
                                            scalar-mul
                                            elementwise-add
                                            elementwise-min
                                            jaccard]]
            [clojure.core :exclude [rand-int]]))


(def mersenne (biginteger (dec (bit-shift-left 1 61))))        ;; large prime
(def max-hash (biginteger (dec (bit-shift-left 1 32))))        ;; max-hash size, used to truncate minhash values
(def seed (:seed env))                                         ;; random number seed
(def perms (:perms env))                                       ;; minhash dimension

(defn- init-hashvalues
  "initializes minhash signature to infinity"
  []
  (vec (repeat perms mersenne)))

(defn- build-permutations
  "builds seeded random number populated vectors to simulate
   the vector permutations a and b"
  []
  (set-random-seed! seed)
  {:a (rand-vec perms mersenne)
   :b (rand-vec perms mersenne)})

;; build seeded vector permutations once. They are the same for every minhash
;; which allows incremental minhashing a single vector at a time.
(defonce permutations (build-permutations))

(defn update-minhash
  "updates minhash with each document feature (token, shingle, n-gram, etc...)
  Tokens are hashed using sha1 hash and truncated at max-hash to allow hashing
  of documents with varying feature sizes. One minhash should be created for
  each document"
  [hashvalues bt]
  (let [hv (get-hash-biginteger bt)
        a (:a permutations)
        b (:b permutations)]
    (-> (scalar-mul a hv)
        (elementwise-add b)
        (scalar-mod mersenne)
        (scalar-and max-hash)
        (elementwise-min hashvalues))))

(defn build-minhash
  "iterates through a document feature vector: ['token-1' token-2' ... 'token-n],
  updating the minhash with each feature. Complete minhash is returned."
  ([bt-vec]
   (build-minhash bt-vec (init-hashvalues)))

  ([[bt & rest] hashvalues]
   (if (nil? bt)
     (vec hashvalues)
     (recur rest (update-minhash hashvalues bt)))))

(defn merge-minhash
  "merges two minhashes together by taking the elementwise minimum between the two
  minhash vectors"
  [minhash1 minhash2]
  (elementwise-min minhash1 minhash2))

