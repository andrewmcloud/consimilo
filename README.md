# consimilo

Consilimo is a library for utilizing lsh-forests to find the top similar items. 

## Installation

```
lein install
```

## Usage

The main methods you are likely to need are all in [`core.clj`](./src/consimilo/core.clj).
Import it with something like:

```clojure
(ns my-awesome.namespace
  (:require [consimilo.core :refer [add-all-to-forest query-forest]]))
```

#### Building the forest

First you need to load all the candidate vectors into an lsh forest.
These vectors can represent any arbitrary information (e.g. terms in document,
metadata about users).

Your vectors need to be in the form of:

```clojure
{:label "name of thing that had this vector"
 :vector ["some" "representation" "of" "vector"]}
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
