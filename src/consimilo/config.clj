(ns consimilo.config
  (:require [config.core :refer [env]]
            [clojure.tools.logging :as log]))

(defonce trees
  (if-let [trees-env (:trees env)]
    trees-env
    (do
      (log/info "Number of trees (:trees) cannot be read from config; Defaulting to 8.")
      8)))

(defonce perms
  (if-let [perms-env (:perms env)]
    perms-env
    (do
      (log/info "Number of permutations (:perms) cannot be read from config; Defaulting to 128.")
      128)))

(defonce seed
  (if-let [seed-env (:seed env)]
    seed-env
    (do
      (log/info "Random number seed (:seed) cannot be read from config; Defaulting to 1.")
      1)))