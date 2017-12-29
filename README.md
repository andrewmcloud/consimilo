# consimilo

### A Clojure library for querying large datasets on similarity

consilimo is a library which utilizes minhashing and locality sensitive hashing, specifically lsh-forests, to support 
top-k similar item queries. Finding similar items across expansive data-sets is a common problem that presents itself 
in many real world applications (e.g. Finding articles from the same source, Plagarism detection, Collaborative Filtering, 
Context Filtering, Document Similarity, etc...). Calculating similarity between a query document and the corpus grows to 
an unwieldy complexity *(n choose 2)* at relatively small corpus sizes. LSH reduces the search space by "hashing" items 
in such a way that collisions occur as a result of similarity. Once the items are hashed and indexed the lsh-forest 
supports a top-k most similar items query of ~*O(log n)*. There is an accuracy tradeoff that comes with the enormous 
increase in query speed.


## Getting Started

The main methods you are likely to need are all in [`core.clj`](./src/consimilo/core.clj).
Import it with something like:

```clojure
(ns my-awesome.namespace
  (:require [consimilo.core :as consimilo]))
```

### Building a forest

First you need to load the candidates vector into an lsh forest. These vector can represent any arbitrary information 
(e.g. tokens in a document, shingled tokens, metadata about users, content interactions, context surrounding 
interactions). The candidate vector must be a collection of maps, each representing an item. The map will have an :id 
key which is used to reference the minhash vector in the forest and a :features key which is a vector containing the 
individual features (e.g ```[{:id id1 :features [feature1 feature2 ... featuren]} ... ]```).

Once your candidates vector is in the correct form, you can add the items to the forest:

#### Adding feature vectors to forest
```clojure
(def my-forest (add-all-to-forest vectors))
```

You can continue to add to this forest by passing it as the first argument to ```add-all-to-forest```.
(Note: upon every call to add-all-to-forest an expensive sort function is called to enable O(log n) queries. It is 
better to add all items to the forest at once, or add batches offline and replace the production forest):

```clojure
(def my-updated-forest (add-all-to-forest forest new-vectors))
```

#### Helper functions - adding strings and files to forest

consimilo provides helper functions for constructing feature vectors from strings and files. By default, a new forest is 
created, stopwords are removed, and the features consist of individual tokens. You may add to an existing forest, 
include stopwords, or shingle the text into n-length token features via optional parameters (:forest :stopwords 
:shingle? :n) The optional parameters are defaulted to :forest (new-forest) :stopwords? true :shingle? false :n 3.

##### Adding documents/strings to forest

Add a collection of strings to a *new* forest, remove stopwords, single token features.
```clojure
(def my-forest (add-strings-to-forest
                 [{:id id1 :features "my sample string 1"}
                  {:id id2 :features "my sample string 2"}]))
```

Add a collection of strings to an *existing* forest, remove stopwords, single token features.
```clojure
(def my-forest (add-strings-to-forest
                 [{:id id1 :features "my sample string 1"}
                  {:id id2 :features "my sample string 2"}]
                 :forest forest))
```

Add a collection of strings to an *existing* forest, *include* stopwords, 4 shingle token features.
```clojure
(def my-updated-forest (add-strings-to-forest
                         [{:id id1 :features "my sample string 1"}
                          {:id id2 :features "my sample string 2"}]
                         :forest forest
                         :stopwords false
                         :shingle? true
                         :n 4))
```

Add a collection of strings to an *existing* forest, *include* stopwords, 4 shingle token features. Shingle length must 
be greather than one and less than feature length, else single token features will be utilized.
```clojure
(def my-updated-forest (add-strings-to-forest
                         [{:id id1 :features "my sample string 1"}
                          {:id id2 :features "my sample string 2"}]
                         :forest forest
                         :stopwords false
                         :shingle? true
                         :n 4))
``` 

##### Adding files to forest
Add a collection of files to a *new* forest, remove stopwords, 4 shingle token features. :id is auto-generated from the 
file name and :features are generated from the extracted text. The same optional parameters available for 
```(add-strings-to-forest)``` are also available for ```(add-files-to-forest)```

```clojure
(def my-forest (add-files-to-forest
                 [Fileobj1 Fileobj2 Fileobj3 Fileobjn]
                 :shingle? true
                 :n 4))
```

### Querying the Forest

Once you have your `forest` built you can query for the top `k` similar entries to
a vector `v` by running:

```clojure
(def results (query-forest my-forest v k))

(println (:top-k results)) ;;returns a list of keys ordered by similarity
(println (:query-hash results)) ;;returns the minhash of the query. Utilized to calculate similarity.
```  

#### Helper functions - querying forest with strings and files

consimilo provides helper functions for querying the forest with strings and files. Queries against strings and files 
should be made using the same tokenization / shingling scheme used to input items in the forest. The two helper 
functions ```(query-stinrg)``` and ```(query-file)``` have optional parameters :stopwords? :shingle? :n. The optional 
parameters are defaulted to the same values as ```(add-stingers-to-forest)``` and ```(add-files-to-forest)```.

##### Query forest with string

```clojure
(def results (query-string my-forest "my query string"))

(println (:top-k results)) ;;returns a list of keys ordered by similarity
(println (:query-hash results)) ;;returns the minhash of the query. Utilized to calculate similarity.
```  
##### Query forest with file

```clojure
(def restuls (query-file my-forest Fileobj))

(println (:top-k results)) ;;returns a list of keys ordered by similarity
(println (:query-hash results) ;;returns the minhash of the query. Utilized to calculate similarity.
  ```
  
##### Query forest and calculate similarity

consimilo provides helper functions for calcuating similarity between the query and top-k results. jaccard, cosine, 
and hamming functions are available. ```(similar-k)``` accepts optional parameters to specify which similarity function 
should be used: :jaccard? :hamming? :cosine?. ```(similar-k)``` returns a hashmap, keys are the top-k ids and vals are 
the similarities.

```clojure
(def sim (similar-k 
           forest
           query
           k
           :cosine? true))

(println sim) ;;{id1 (cosine-distance query id1) ... idk (cosine-distance query idk}
```
 


