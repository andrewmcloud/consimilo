(ns consimilo.minhash
  (:require [consimilo.random-seed :refer [set-random-seed!
                                           rand-vec]]
            [consimilo.sha1 :refer [get-hash-bigint]]
            [config.core :refer [env]]
            [consimilo.minhash-util :refer [elementwise-add
                                            elementwise-min
                                            scalar-mod
                                            scalar-mul]]
            [clojure.core :exclude [rand-int]]
            [clojure.tools.logging :as log]))

;; prime number larger than sha1 hash
(def mersenne (dec (.shiftLeft (biginteger 1) (biginteger 181))))

;; random number seed
(def seed (if (:seed env)
            (:seed env)
            (do
              (log/warn "Random number seed is not configured; please ensure :seed is in the config.edn file.
                        Defaulting to 1.")
              1)))

;; minhash dimension
(def perms (if (:perms env)
             (:perms env)
             (do
               (log/warn "Number of permutations cannot be configured; please ensure :perms is in the config.edn file.
                         Defaulting to 128.")
               128)))

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
  [hashvalues feature]
  (let [hv (get-hash-bigint (str feature))
        a (:a permutations)
        b (:b permutations)]
    (-> (scalar-mul a hv)
        (elementwise-add b)
        (scalar-mod mersenne)
        (elementwise-min hashvalues))))

(defn build-minhash
  "iterates through a document feature collection: ['token-1' token-2' ... 'token-n],
  updating the minhash with each feature. Complete minhash is returned."
  ([feature-coll]
   (build-minhash feature-coll (init-hashvalues)))

  ([[feature & features] hashvalues]
   (if (nil? feature)
     (vec hashvalues)
     (recur features (update-minhash hashvalues feature)))))

(defn merge-minhash
  "merges two minhashes together by taking the elementwise minimum between the two
  minhash vectors"
  [minhash1 minhash2]
  (elementwise-min minhash1 minhash2))

