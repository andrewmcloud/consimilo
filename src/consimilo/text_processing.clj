(ns consimilo.text-processing
  (:require [opennlp.nlp :refer [make-tokenizer]]
            [pantomime.mime :refer [mime-type-of]]
            [pantomime.extract :as extract]
            [clojure.java.io :as io]
            [clojure.string :refer [lower-case]]
            [config.core :refer [env]]
            [clojure.tools.logging :as log]))

(def tokenize (make-tokenizer (io/resource "en-token.bin")))

(defn tokenize-text
  [text]
  (->> (lower-case text)
       tokenize
       set
       vec))

(defn shingle
  ([text-vec n]
   (shingle text-vec n []))
  ([[first & rest] n coll]
   (let [k (dec n)]
     (if (not= k (count (take k rest)))
       (vec (set (map lower-case coll)))
       (recur rest n (conj coll (->> rest
                                     (take k)
                                     (concat [first])
                                     (apply str))))))))

(defn- parse-file-to-text
  "Parse pdf calls extract/parse and catches an IndexOutOfBounds exception that is thrown by tika on rare occasion."
  [file]
  (try
    (extract/parse file)
    (catch IndexOutOfBoundsException e
      (log/warn "Unable to extract text from pdf - filename: " (.getName file)))))

(defn extract-text
  "Return extracted text by file content (as `java.io.File`)."
  [file_obj]
  (:text (parse-file-to-text file_obj)))
