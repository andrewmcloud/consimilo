(ns consimilo.text-processing
  (:require [opennlp.nlp :refer [make-tokenizer]]
            [pantomime.extract :as extract]
            [clojure.java.io :as io]
            [clojure.string :refer [lower-case
                                    split-lines]]
            [clojure.tools.logging :as log]))

(def ^:private tokenize (make-tokenizer (io/resource "en-token.bin")))
(def ^:private stopwords (set (split-lines (slurp (io/resource "stopwords.txt")))))

(defn- remove-stopwords
  "If remove-stopwords?: returns tokenized-text with stopwords removed, else: returns tokenized-text unaltered"
  [remove-stopwords? tokenized-text]
  (if remove-stopwords?
    (remove stopwords tokenized-text)
    tokenized-text))

(defn tokenize-text
  "Tokenizes a string of text. If remove-stopwords?: removes stopwords from token collection"
  [text & {:keys [remove-stopwords?] :or {remove-stopwords? true}}]
  (->> (lower-case text)
       tokenize
       (remove-stopwords remove-stopwords?)))

;;Not currently used
(defn shingle
  "Generates contiguous sequences of tokens of length n, may be a better gauge of similarity when using consimilo
  to query a text corpus for similarity. Generate tokenized-text via consimilo.text-processing/tokenize-text"
  ([tokenized-text n]
   (if (and (> n 1) (<= n (count tokenized-text)))
     (shingle tokenized-text n [])
     (do
       (log/warn "Invalid shingle size. Shingle size must be (1 < n <= tokenized-text) returning tokenized-text")
       tokenized-text)))
  ([[first & rest] n coll]
   (let [k (dec n)]
     (if (not= k (count (take k rest)))
       coll
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
