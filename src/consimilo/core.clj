(ns consimilo.core
  (:require [consimilo.lsh-forest :as f]
            [consimilo.lsh-util :as util]
            [consimilo.minhash :as mh]
            [consimilo.minhash-util :as mhu]
            [consimilo.lsh-query :as q]
            [consimilo.text-processing :as text]
            [taoensso.nippy :as nippy]
            [clojure.tools.logging :as log]))

(defn add-all-to-forest
  "Adds each vector in `feature-coll` to an lsh forest and returns the forest.
  If you want to add the `feature-coll` to an existing `forest` pass the forest as the first argument.
  Each item of `feature-coll` should be a map with :id and :features entries.
  The :id is the identifier for the minhash vector that will be returned upon query of the forest.
  This id can be utilized to lookup the minhash vector in the :keys hashmap of the forest.
  The :features is a collection of strings which will be utilized to create the minhash vector
  (e.g. in the case of a document, the :features could be tokens or n-grams).

  Note: items should be loaded into the forest as few times as possible in large chunks. An expensive
  sort called after items are added to the forest to enable ~log(n) queries."
  ([feature-coll]
   (add-all-to-forest (f/new-forest) feature-coll))
  ([forest feature-coll]
   (if (util/valid-input? feature-coll coll?)
     (do
       (dorun (pmap #(f/add-lsh! forest (:id %) (mh/build-minhash (:features %))) feature-coll))
       (f/index! forest)
       forest)
     (log/warn "invalid input, feature-coll must be a collection of maps, each having keys :id and :features;
               :features must be a collection"))))

(defn add-strings-to-forest
  "Convenience method for processing documents. Each item of feature-coll should be a map with
  :id and :features entries. The :id is the identifier for the minhash vector stored in the forest.
  The :features is a string which will be tokenized into features per the optional
  parameters. The feature vector will be minhashed and inserted into the lsh-forest.

  Optional Keyword Arguments: :forest - add to an existing forest; default: create new forest

  Note: items should be loaded into the forest as few times as possible in large chunks. An expensive
  sort called after items are added to the forest to enable ~log(n) queries."

  [feature-coll & {:keys [forest] :or {forest (f/new-forest)}}]
  (if (util/valid-input? feature-coll string?)
    (add-all-to-forest forest
                       (map #(assoc % :features (text/tokenize-text (:features %))) feature-coll))
    (log/warn "invalid input, feature-coll must be a collection of maps, each having keys :id and :features;
               :features must be a string")))

(defn add-files-to-forest
  "Convenience method for processing files. Files should be a collection of File objects.
  The :id used for entry into the forest will be generated from the file name. The :features will
  be generated by extracting the text from each file and tokenizing and/or shingling per the optional
  parameters. The feature vector is minhashed and inserted into the lsh-forest.

  Optional Keyword Arguments: :forest - add to an existing forest; default: create new forest

  Note: items should be loaded into the forest as few times as possible in large chunks. An expensive
  sort called after items are added to the forest to enable ~log(n) queries."
  [files & {:keys [forest] :or {forest (f/new-forest)}}]
  (if (util/valid-input-add-files? files)
    (add-strings-to-forest (map (fn [f] {:id (.getName f)
                                         :features (text/extract-text f)})
                                files)
                           :forest forest)
    (log/warn "invalid input, files must be a collection of file objects")))

(defn query-forest
  "Finds the closest `k` vectors to vector `v` stored in the `forest`."
  [forest k v]
  (let [minhash (mh/build-minhash v)]
    {:top-k (q/query forest k minhash) :query-hash minhash}))

(defn query-string
  "Convenience method for querying the forest for top-k similar strings. forest is the forest to be
  queried. string will be converted to a feature vector through tokenization / shingling per the optional
  parameters. The feature vector is minhashed and used to query the forest. K is the number of results
  (top-k most similar items)."
  [forest k string]
  (query-forest forest k (text/tokenize-text string)))

(defn query-file
  "Convenience method for querying the forest for top-k similar files. Forest is the forest to be
  queried. File is converted to a feature vector through text-extraction, tokenizating / shingling
  per the optional arguments. The feature vector is minhashed and used to query the forest. k is the number
  of results (top-k most similar items)."
  [forest k file]
  (query-string forest k (text/extract-text file)))

(defn get-sim-fn
  [key]
  (condp = key
    :jaccard mhu/jaccard-similarity
    :cosine mhu/cosine-distance
    :hamming mhu/hamming-distance))

(defmulti similarity-k
  "Query forest for top-k items, returns a hashmap: {item-key1 sim-fn-result1 item-key-k sim-fn-result-k}. Available
  similarity functions are Jaccard similarity, cosine distance, and Hamming distance. sim-fn is defaulted to :jaccard,
  but can be overridden by passing the optional :sim-fn key and :jaccard, :cosine, or :hamming. similarity-k Dispatches
  based on input: string, file, or feature-vector."
  (fn [forest k input & {:keys [sim-fn] :or {sim-fn :jaccard}}]
    (condp #(%1 %2) input
      coll? :feature-vec
      string? :string
      :file)))

(defmethod similarity-k :string
  [forest k string & {:keys [sim-fn] :or {sim-fn :jaccard}}]
  (let [return (query-string forest k string)
        f (get-sim-fn sim-fn)]
    (mhu/zip-similarity forest return f)))

(defmethod similarity-k :file
  [forest k file & {:keys [sim-fn] :or {sim-fn :jaccard}}]
  (let [return (query-file forest k file)
        f (get-sim-fn sim-fn)]
    (mhu/zip-similarity forest return f)))

(defmethod similarity-k :feature-vec
  [forest k feature-vector & {:keys [sim-fn] :or {sim-fn :jaccard}}]
  (let [return (query-forest forest k feature-vector)
        f (get-sim-fn sim-fn)]
    (mhu/zip-similarity forest return f)))

(defn freeze-forest
  "Serializes forest and saves to a file. Forest should be created using one of the add-*-to-forest functions.
  file-path should be a string representing the filepath. Returns the byte-array representation of the serialize
  object and creates a file containing the byte-string representation of the serialized object."
  [forest file-path]
  (nippy/freeze-to-file file-path @forest))

(defn thaw-forest
  "Deserializes forest from file. file-path should be a string representing the filepath of the serialized object.
  Returns an lsh-forest."
  [file-path]
  (atom (nippy/thaw-from-file file-path)))