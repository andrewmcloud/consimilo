(ns consimilo.lsh-forest-test
  (:require [clojure.test :refer :all]
            [consimilo.lsh-forest :refer :all]
            [consimilo.lsh-util :refer [v=v keyword-int]]
            [consimilo.minhash :refer [build-minhash]]
            [consimilo.lsh-state :refer [sort-tree]]))

(def minhash1 (build-minhash ["1" "2" "3"]))
(def minhash2 (build-minhash ["1" "2" "10"]))
(def minhash3 (build-minhash ["32" "64" "128"]))

(deftest populate-hashtables-test
  (testing "updates might-atom :hashtables entry"
    (let [private-populate-hashtables #'consimilo.lsh-state/populate-hastables!
          forest (atom {})]
      (private-populate-hashtables forest "a" minhash1)
      (is (not (empty? (get-in @forest [:hashtables :0])))))))

(deftest populate-keys-test
  (testing "updates might-atom :keys entry"
    (let [private-populate-keys #'consimilo.lsh-state/populate-keys!
          forest (atom {})]
      (private-populate-keys forest "a" minhash1)
      (is (not (empty? (get-in @forest [:keys :a])))))))

(deftest lsh-forest-integration-test
  (testing "lsh forest returns best match on query"
    (let [forest (new-forest)]
      (dorun (map-indexed #(add-lsh! forest (str (inc %)) %2) [minhash1 minhash2 minhash3]))
      (index! forest)
      (is (= :1 (keyword (first (query-forest forest minhash1 1))))))))
