(ns consimilo.random-seed
  (:refer-clojure :exclude [rand rand-int])
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

(defn rand-biginteger
  [n]
  (biginteger (rand n)))

(defn rand-vec
  [n max-range]
  (repeatedly n #(rand-biginteger max-range)))
