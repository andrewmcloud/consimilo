# consimilo
[![Build Status](https://travis-ci.org/andrewmcloud/consimilo.svg?branch=master)](https://travis-ci.org/andrewmcloud/consimilo)
### A Clojure library for querying large data-sets on similarity

consimilo is a library that utilizes locality sensitive hashing (implemented as lsh-forest) and minhashing, to support 
*top-k* similar item queries. Finding similar items across expansive data-sets is a common problem that presents itself 
in many real world applications (e.g. finding articles from the same source, plagiarism detection, collaborative 
filtering, context filtering, document similarity, etc...). Searching a corpus for *top-k* similary items quickly grows 
to an unwieldy complexity at relatively small corpus sizes *(n choose 2)*. LSH reduces the search space by "hashing" 
items in such a way that collisions occur as a result of similarity. Once the items are hashed and indexed the 
lsh-forest supports a *top-k* most similar items query of ~*O(log n)*. There is an accuracy trade-off that comes with 
the enormous increase in query speed. More information can be found in chapter 3 of 
[Mining Massive Datasets](http://infolab.stanford.edu/~ullman/mmds/ch3.pdf).

## Getting Started

The main methods you are likely to need are all located in [`core.clj`](./src/consimilo/core.clj). 
Import it with something like:

```clojure
(ns my-ns (:require [consimilo.core :as consimilo]))
```

### Building a forest

First you need to load the candidates vector into an lsh-forest. This vector can represent any arbitrary information 
(e.g. tokens in a document, ngrams, metadata about users, content interactions, context surrounding 
interactions). The candidates vector must be a collection of maps, each representing an item. The map will have an 
`:id` key which is used to reference the minhash vector in the forest and a `:features` key which is a vector 
containing the individual features. `[{:id id1 :features [feature1 feature2 ... featuren]} ... ]`

#### Adding feature vectors to a forest

Once your candidates vector is in the correct form, you can add the items to the forest:

```clojure
(def my-forest (consimilo/add-all-to-forest candidates-vector))           ;;creates new forest, my-forest
```

You can continue to add to this forest by passing it as the first argument to `add-all-to-forest`. The forest data 
structure is stored in an atom, so the existing forest is modified in place. 

Note: upon every call to `add-all-to-forest` an expensive sort function is called to enable *O(log n)* queries. It is 
better to add all items to the forest at once or in the case of a live system, add new items to the forest in batches 
offline and replace the production forest.

```clojure
(consimilo/add-all-to-forest my-forest new-candidates-vector)             ;;updates my-forest in place
```

#### Adding strings and files to a forest (helper functions)

consimilo provides helper functions for constructing feature vectors from strings and files. By default, a new forest 
is created and stopwords are removed. You may add to an existing forest and/or include stopwords via optional 
parameters `:forest` `:stopwords`. The optional parameters are defaulted to `:forest (new-forest)` `:remove-stopwords? 
true`.

##### Adding documents/strings to a forest

To add a collection of strings to a **new** forest and **remove** stopwords:

```clojure
(def my-forest (consimilo/add-strings-to-forest
                 [{:id id1 :features "my sample string 1"}
                  {:id id2 :features "my sample string 2"}]))
```

To add a collection of strings to an **existing** forest and **do not remove** stopwords: 

```clojure
(consimilo/add-strings-to-forest [{:id id1 :features "my sample string 1"}
                                  {:id id2 :features "my sample string 2"}]
                                 :forest my-forest                        
                                 :remove-stopwords? false))               ;;updates my-forest in place
```

##### Adding files to a forest

To add a collection of files to a **new** forest and **remove** stopwords:

```clojure
(def my-forest (consimilo/add-files-to-forest
                 [FileObj-1 FileObj-2 FileObj-3 FileObj-n]))              ;;creates new forest, my-forest
```

Note: when calling `add-files-to-forest` `:id` is auto-generated from the file name and `:features` are generated from 
the extracted text. The same optional parameters available for `add-strings-to-forest` are also available for 
`add-files-to-forest`.

### Querying a forest

Once you have your forest `my-forest` built, you can query for `k` most similar items to feature-vector `v` by running:

```clojure
(def results (consimilo/query-forest my-forest k v))

(println (:top-k results)) ;;returns a list of keys ordered by similarity
(println (:query-hash results)) ;;returns the minhash of the query. Utilized to calculate similarity.
```  

#### Querying a forest with strings and files (helper functions)

consimilo provides helper functions for querying the forest with strings and files. The helper functions `query-string` 
and `query-file` have an optional parameter `:remove-stopwords?` which is defaulted `true`, removing stopwords. Queries 
against strings and files should be made using the same tokenization scheme used to input items in the forest 
(stopwords present or removed).

##### Querying my-forest with a string

```clojure
(def results (consimilo/query-string my-forest k "my query string"))

(println (:top-k results)) ;;returns a list of keys ordered by similarity
(println (:query-hash results)) ;;returns the minhash of the query. Utilized to calculate similarity.
```  

##### Querying my-forest with a file

```clojure
(def results (consimilo/query-file my-forest k Fileobj))

(println (:top-k results)) ;;returns a list of keys ordered by similarity
(println (:query-hash results)) ;;returns the minhash of the query. Utilized to calculate similarity.
  ```
  
#### Querying a forest with strings, files, or feature-vectors and calculating similarity

consimilo provides functions for calculating approximate distance / similarity between the query and *top-k* results. 
The function `similar-k` accepts optional parameters to specify which distance / similarity function should be used. 
For calculating Jaccard similarity, use: `:sim-fn :jaccard`, for calculating Hamming distance, use: `:sim-fn :hamming`, 
and for calculating cosine distance, use: `:sim-fn :cosine`. `similar-k` returns a hash-map, `keys` are the *top-k* ids 
and `vals` are the similarity / distance. As with the other query functions, queries against strings and files 
should be made using the same tokenization scheme used to input the items in the forest (stopwords present or removed).

```clojure
(def similar-items (consimilo/similar-k 
                     my-forest
                     k
                     query
                     :sim-fn :cosine))

(println similar-items) ;;{id1 (cosine-distance(query id1)) ... idk (cosine-distance (query idk))}
```

### Saving and Loading lsh-forests

consimilo uses [Nippy](https://github.com/ptaoussanis/nippy) to provide simple robust serialization / deserialization 
of your lsh-forests.

To serialize and save my-forest to a file:
```clojure
(consimilo/freeze-forest my-forest "/tmp/my-saved-forest")
```

To load a my-forest from a file:
```clojure
(def my-forest (consimilo/thaw-forest "/tmp/my-saved-forest"))
```

## Contributions / Issues

Please use the project's GitHub issues page for questions, ideas, etc. Pull requests are welcome.

## License

Copyright 2018 Andrew McLoud

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
