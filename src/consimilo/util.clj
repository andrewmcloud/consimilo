(ns consimilo.util
  (:require [clojure.core.matrix :as m]))

(m/set-current-implementation :vectorz)

(defn- elementwise
  [f, vec, k]
  (m/emap! (fn [e] (f (int e) k)) vec))

(defn elementwise-and
  [vec, k]
  (m/emap (fn [e] (.and (biginteger e) (biginteger k))) vec))

(defn elementwise-mod
  [vec, k]
  (elementwise mod vec k))
