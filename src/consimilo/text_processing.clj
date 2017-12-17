(ns consimilo.text-processing
  (:require [corenlp :refer [tokenize]]))

(defn tokenize-text
  [text]
  (->>(tokenize text)
      (map :token)))

(defn shingle
  ([text-vec n]
   (shingle text-vec n []))
  ([[first & rest] n coll]
   (let [k (dec n)]
     (if (not= k (count (take k rest)))
       coll
       (recur rest n (conj coll (->> rest
                                     (take k)
                                     (concat [first])
                                     (apply str))))))))
