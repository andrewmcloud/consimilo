(ns consimilo.minhash
  (:require [consimilo.random-seed :as rseed]
            [consimilo.sha1 :as sha]
            [consimilo.config :as config]
            [consimilo.minhash-util :as util]
            [clojure.core :exclude [rand-int]]
            [clojure.tools.logging :as log]))

;; prime number larger than sha1 hash
(def large-prime 3064991081731777716716694054300618367237478244367416721N)

(defn- init-hashvalues
  "initializes minhash signature to infinity"
  []
  (vec (repeat config/perms large-prime)))

(defn- build-permutations
  "builds seeded random number populated vectors to simulate
   the vector permutations a and b"
  []
  (rseed/set-random-seed! config/seed)
  {:a (rseed/rand-vec config/perms large-prime)
   :b (rseed/rand-vec config/perms large-prime)})

;; build seeded vector permutations once. They are the same for every minhash
;; which allows incremental minhashing a single vector at a time.
(defonce permutations (build-permutations))

(defn update-minhash
  "updates minhash with each document feature (token, shingle, n-gram, etc...)
  Tokens are hashed using sha1 hash and truncated at max-hash to allow hashing
  of documents with varying feature sizes. One minhash should be created for
  each document"
  [hashvalues feature]
  (let [hv (sha/get-hash-bigint (str feature))
        a (:a permutations)
        b (:b permutations)]
    (-> (util/scalar-mul a hv)
        (util/elementwise-add b)
        (util/scalar-mod large-prime)
        (util/elementwise-min hashvalues))))

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
  (util/elementwise-min minhash1 minhash2))

