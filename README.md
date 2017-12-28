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

#### Building the forest

First you need to load all the candidate vectors into an lsh forest. These vectors can represent any arbitrary information 
(e.g. tokens in a document, shingled tokens, metadata about users, content interactions, context surrounding interactions).

consimilo provides helper methods for constructing feature vectors from strings and files; by default, a new forest is created,
stopwords are removed, the features consist of individual tokens. You may add to an existing forest, include stopwords, or 
shingle the text into n-length token features via optional parameters:

####Strings

Add a collection of strings to a *new* forest, remove stopwords, single token features.
```clojure
(add-strings-to-forest
  [{:id id1 :features "my sample string 1"}
   {:id id2 :features "my sample string 2"}
   {:id id3 :features "my sample string 3"}
   {:id id4 :features "my sample string 4"}])
```

Add a collection of strings to an *existing* forest, remove stopwords, single token features.
```clojure
(add-strings-to-forest
  [{:id id1 :features "my sample string 1"}
   {:id id2 :features "my sample string 2"}
   {:id id3 :features "my sample string 3"}
   {:id id4 :features "my sample string 4"}]
   :forest forest)
```

Add a collection of strings to an *existing* forest, *include* stopwords, 3 shingle token features.
```clojure
(add-strings-to-forest
  [{:id id1 :features "my sample string 1"}
   {:id id2 :features "my sample string 2"}
   {:id id3 :features "my sample string 3"}
   {:id id4 :features "my sample string 4"}]
   :forest forest
   :stopwords false
   :shingle? true)
```

Add a collection of strings to an *existing* forest, *include* stopwords, 4 shingle token features. Shingle length must 
be greather than one and less than feature length, else single token features will be utilized.
```clojure
(add-strings-to-forest
  [{:id id1 :features "my sample string 1"}
   {:id id2 :features "my sample string 2"}
   {:id id3 :features "my sample string 3"}
   {:id id4 :features "my sample string 4"}]
   :forest forest
   :stopwords false
   :shingle? true
   :n 5)
``` 

####Files
Add a collection of files to a *new* forest, remove stopwords, 4 shingle token features. :id is auto-generated from the 
file name and :features are generated from the extracted text. The same optional parameters available for 
```(add-strings-to-forest)``` are available for ```(add-files-to-forest)```

```clojure
(add-files-to-forest
  [Fileobj1 Fileobj2 Fileobj3 Fileobjn]
  :shingle? true
  :n 4)
```

Once you have a collection of `vectors` that look like that you can call:

```clojure
(def forest (add-all-to-forest vectors))
```

You can continue to add to this forest by passing it as the first argument to
`add-all-to-forest` like:

```clojure
(def updated-forest (add-all-to-forest forest new-vectors))
```

#### Querying the Forest

Once you have your `forest` built you can query for the top `k` similar entries to
a vector `v` by running:

```clojure
(query-forest forest v k)
```
