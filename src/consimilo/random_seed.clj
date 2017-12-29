(ns consimilo.random-seed
  (:refer-clojure :exclude [rand-int
                            rand])
  (:import (java.util Random)))

(defonce r (Random.))

(defn set-random-seed!
  "Sets the random number generator seed"
  [seed]
  (.setSeed r seed))

(defn rand
  "Overloads the clojure rand function to with a seeded implementation
  utilizing java.util.Random"
  ([] (.nextFloat r))
  ([n] (* n (rand))))

(defn rand-bigint
  "returns a random number of type bigint, may be seeded by calling set-random-seed! prior
  to calling rand-bigint"
  [n]
  (bigint (rand n)))

(defn rand-vec
  "returns a vector of length n random numbers range [0 - max-range], may be seeded by calling
  set-random-seed! prior to calling rand-bigint"
  [n max-range]
  (repeatedly n #(rand-bigint max-range)))
