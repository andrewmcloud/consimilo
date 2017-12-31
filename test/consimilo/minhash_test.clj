(ns consimilo.minhash-test
  (:require [clojure.test :refer :all]
            [clojure.set :refer [intersection]]
            [consimilo.minhash :refer :all])
  (:import (clojure.lang BigInt)))

(defn build-bigint-coll
  [coll]
  (map bigint coll))

(def minhash-1 (build-bigint-coll '(1 2 3 4 5 6 7 8 9)))
(def minhash-2 (build-bigint-coll '(9 8 7 6 5 4 3 2 1)))
(def minhash-3 (build-bigint-coll '(1 2 3 4 5 4 3 2 1)))

(deftest init-hashvalues-test
  (let [private-init-hashvals #'consimilo.minhash/init-hashvalues
        hashvals (private-init-hashvals)]
    (testing "init-hahsvalues returns a collection of mersenne primes"
      (is (every? #(= mersenne %) hashvals)))
    (testing "init-hashvalues returns a collection of type bigint"
      (is (every? #(instance? BigInt %) hashvals)))
    (testing "init-hashvalues returns a collection of length perms"
      (is (= perms (count hashvals))))))

(deftest build-permutations-test
  (let [private-build-permutations #'consimilo.minhash/build-permutations
        p (private-build-permutations)]
    (testing "keys :a and :b are not nil in permutations map"
      (is (not (nil? (:a p))))
      (is (not (nil? (:b p)))))
    (testing "keys :a and :b are collections of length perms"
      (is (= perms (count (:a p))))
      (is (= perms (count (:b p)))))
    (testing "keys :a and :b are unique collections"
      (is (not= perms (count (intersection (set (:a p)) (set (:b p)))))))))

(deftest build-minhash-test
  (let [minhash (build-minhash ["my" "name" "is" "andrew"])]
    (testing "resulting minhash is length perms"
      (is (= perms (count minhash))))
    (testing "elements in minhash collection are of type minhash"
      (is (instance? BigInt (first minhash)))
      (is (instance? BigInt (last minhash))))))

(deftest merge-minhash-test
  (testing "testing merging two minhash vectors together."
    (is (= minhash-3 (merge-minhash minhash-1 minhash-2)))))