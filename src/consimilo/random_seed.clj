(ns consimilo.random-seed
  (:refer-clojure :exclude [rand rand-int]))

(defonce r (java.util.Random.))

(defn set-random-seed!
  [seed]
  (.setSeed r seed))

(defn rand
  ([] (.nextFloat r))
  ([n] (* n (rand))))

(defn rand-int
  [n]
  (biginteger (rand n)))

(defn rand-vec
  [n max-range]
  (repeatedly n #(rand-int max-range)))
