(ns consimilo.minhash
  (:require [clojure.core.matrix :as m]))

(m/set-current-implementation :vectorz)

(def mersenne (- (bit-shift-left 1 61) 1))
(def max-hash (- (bit-shift-left 1 32) 1))
(def hash-range (bit-shift-left 1 32))

(defn- init_hashvalues
  ""
  [permutations]
  (-> (m/new-vector permutations)
      (m/fill 1)
      (m/mul max-hash)))

