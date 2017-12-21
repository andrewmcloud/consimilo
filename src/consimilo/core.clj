(ns consimilo.core
  (:require
    [consimilo.lsh-forest :refer [add-lsh! index! new-forest]]
    [consimilo.minhash :refer [build-minhash]]
    [consimilo.lsh-query :refer [query]]
    [consimilo.text-processing :refer [extract-text shingle tokenize]])
  (:import (clojure.lang IAtom)))

(defn add-all-to-forest
  "Adds each vector in `coll` to an lsh forest and returns the forest.
  If you want to add the `coll` to an existing `forest` pass the forest as the first argument.
  Each item of `coll` should be a map with :label and :vector entries.
  The :label is the identifier for the vector that will be returned upon query of the forest.
  The :vector is a ???." ;TODO: describe vector better
  ([coll]
   (add-all-to-forest (new-forest) coll))
  ([forest coll]
   (dorun (pmap #(add-lsh! forest (:label %) (build-minhash (:vector %))) coll))
   (index! forest)
   forest))

(defn query-forest
  "Finds the closts `k` vectors to vector `v` stored in the `forest`."
  [forest v k]
  (query forest (build-minhash v) k))

(defn add-strings-to-forest
  [strings & {:keys [forest shingle? n] :or {forest (new-forest) shingle? false n 3}}]
  (add-all-to-forest forest
                     (map #(assoc % :vector
                             (if shingle?
                               (shingle (tokenize (:vector %)) n)
                               (tokenize (:vector %))))
                          strings)))

(defn query-string
  [forest string k & {:keys [shingle? n] :or {shingle? false n 3}}]
  (query-forest forest
                (if shingle?
                  (shingle (tokenize string) n)
                  (tokenize string))
                k))

(defn add-files-to-forest
  [files & {:keys [forest shingle? n] :or {forest (new-forest) shingle? false n 3}}]
  (add-strings-to-forest (map (fn [f] {:label (.getName f)
                                       :vector (extract-text f)})
                              files)
                         :shingle? shingle?
                         :n n))

(defn query-file
  [forest file k & {:keys [shingle? n] :or {shingle? false n 3}}]
  (query-string forest
                (extract-text file)
                k))
