(ns consimilo.core
  (:require
            [consimilo.lsh-forest :refer [add-lsh! index! new-forest]]
            [consimilo.minhash :refer [build-minhash]]
            [consimilo.lsh-query :refer [query]]))

(defn add-all-to-forest
  "Adds each vector in `coll` to an lsh forest and returns the forest.
  If you want to add the `coll` to an existing `forest` pass the forest as the first argument.
  Each item of `coll` should be a map with :label and :vector entries.
  The :label is the identifier for the vector that will be returned upon query of the forest.
  The :vector is a ???." ;TODO: describe vector better
  ([coll]
   (add-all-to-forest (new-forest) coll))
  ([forest coll]
   (run! #(add-lsh! forest (:label %) (build-minhash (:vector %))) coll)
   (index! forest)
   forest))

(defn query-forest
  "Finds the closts `k` vectors to vector `v` stored in the `forest`."
  [forest v k]
  (query forest (build-minhash v) k))
