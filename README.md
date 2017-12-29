# consimilo

### A Clojure library for querying large data-sets for similarity

consimilo is a library which utilizes locality sensitive hashing (lsh-forest) and minhashing, to support 
*top-k* similar item queries. Finding similar items across expansive data-sets is a common problem that presents itself 
in many real world applications (e.g. finding articles from the same source, plagiarism detection, collaborative filtering, 
context filtering, document similarity, etc...). Calculating similarity between a query document and the corpus grows to 
an unwieldy complexity *(n choose 2)* at relatively small corpus sizes. LSH reduces the search space by "hashing" items 
in such a way that collisions occur as a result of similarity. Once the items are hashed and indexed the lsh-forest 
supports a *top-k* most similar items query of ~*O(log n)*. There is an accuracy trade-off that comes with the enormous 
increase in query speed. More information can be found in chapter 3 of 
[Mining Massive Datasets](http://infolab.stanford.edu/~ullman/mmds/ch3.pdf).


## Getting Started

The main methods you are likely to need are all in [`core.clj`](./src/consimilo/core.clj).
Import it with something like:

```clojure
(ns my-awesome.namespace
  (:require [consimilo.core :as consimilo]))
```

### Building a forest

First you need to load the candidates vector into an lsh-forest. This vector can represent any arbitrary information 
(e.g. tokens in a document, shingled tokens, metadata about users, content interactions, context surrounding 
interactions). The candidates vector must be a collection of maps, each representing an item. The map will have an 
`:id` key which is used to reference the minhash vector in the forest and a `:features` key which is a vector 
containing the individual features. `[{:id id1 :features [feature1 feature2 ... featuren]} ... ]`

#### Adding feature vectors to forest

Once your candidates vector is in the correct form, you can add the items to the forest:

```clojure
(def my-forest (add-all-to-forest candidates-vector))
```

You can continue to add to this forest by passing it as the first argument to `add-all-to-forest`.
Note: upon every call to `add-all-to-forest` an expensive sort function is called to enable *O(log n)* queries. It is 
better to add all items to the forest at once or in the case of a live system, add new items to the forest in batches 
offline and replace the production forest.

```clojure
(def my-updated-forest (add-all-to-forest forest new-candidates-vector))
```

#### Adding strings and files to forest (helper functions)

consimilo provides helper functions for constructing feature vectors from strings and files. By default, a new forest is 
created, stopwords are removed, and the features consist of individual tokens. You may add to an existing forest, 
include stopwords, or shingle the text into n-length token features via optional parameters `:forest` `:stopwords` 
`:shingle?` `:n`). The optional parameters are defaulted to `:forest (new-forest)` `:stopwords? true` `:shingle? false` 
`:n 3`.

##### Adding documents/strings to forest

To add a collection of strings to a *new* forest, *remove* stopwords, *single token* features:

```clojure
(def my-forest (add-strings-to-forest
                 [{:id id1 :features "my sample string 1"}
                  {:id id2 :features "my sample string 2"}]))
```

To add a collection of strings to an *existing* forest, *do not remove* stopwords, *4 shingle* token features: 

```clojure
(def my-updated-forest (add-strings-to-forest
                         [{:id id1 :features "my sample string 1"}
                          {:id id2 :features "my sample string 2"}]
                         :forest forest
                         :stopwords? false
                         :shingle? true
                         :n 4))
``` 
Note: Shingle length must be greater than one and less than feature length, else single token features will be utilized. 
Shingle length can be tuned to based on the similarity desired.

##### Adding files to forest
To add a collection of files to a *new* forest, *remove* stopwords, *4 shingle* token features:

```clojure
(def my-forest (add-files-to-forest
                 [FileObj1 FileObj2 FileObj3 FileObjn]
                 :shingle? true
                 :n 4))
```
Note: when calling `add-files-to-forest` `:id` is auto-generated from the file name and `:features` are generated from 
the extracted text. The same optional parameters available for `add-strings-to-forest` are also available for 
`add-files-to-forest`.

### Querying the Forest

Once you have your `my-forest` built, you can query for the `top-k` similar entries to
a vector `v` by running:

```clojure
(def results (query-forest my-forest v top-k))

(println (:top-k results)) ;;returns a list of keys ordered by similarity
(println (:query-hash results)) ;;returns the minhash of the query. Utilized to calculate similarity.
```  

#### Querying forest with strings and files (helper functions)

consimilo provides helper functions for querying the forest with strings and files. Queries against strings and files 
should be made using the same tokenization / shingling scheme used to input items in the forest. The three helper 
functions `query-string`, `query-file`, and `similar-k` have optional parameters `:stopwords?` `:shingle?` `:n`. The 
optional parameters are defaulted to the same values as `add-strings-to-forest` and `add-files-to-forest`.

##### Querying forest with string

```clojure
(def results (query-string my-forest "my query string"))

(println (:top-k results)) ;;returns a list of keys ordered by similarity
(println (:query-hash results)) ;;returns the minhash of the query. Utilized to calculate similarity.
```  
##### Querying forest with file

```clojure
(def restuls (query-file my-forest Fileobj))

(println (:top-k results)) ;;returns a list of keys ordered by similarity
(println (:query-hash results)) ;;returns the minhash of the query. Utilized to calculate similarity.
  ```
  
##### Querying forest and calculate similarity

consimilo provides helper functions for calculating distance / similarity between the query and *top-k* results. The 
function `similar-k` accepts optional parameters to specify which distance / similarity function should be used. 
For calculating Jaccard similarity, use: `:jaccard? true`, for calculating Hamming distance, use: `:hamming? :true`, 
and for calculating cosine distance, use: `:cosine?`. `similar-k` returns a hashmap, `keys` are the *top-k* ids and 
`vals` are the similarity scores.

```clojure
(def sim (similar-k 
           forest
           query
           k
           :cosine? true))

(println sim) ;;{id1 (cosine-distance query id1) ... idk (cosine-distance query idk}