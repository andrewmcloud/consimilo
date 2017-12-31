(ns consimilo.minhash-util-test
  (:require [clojure.test :refer :all]
            [consimilo.minhash-util :refer :all])
  (:import (clojure.lang BigInt)))

(defn bigint_vec
  [n val]
  (repeat n (bigint val)))

(def big-coll-1 (bigint_vec 3 3))
(def big-coll-2 (bigint_vec 3 10))
(def big-coll-3 [(bigint 3) (bigint 3) (bigint 2)])

(deftest scalar-mod-test
  (let [result (scalar-mod big-coll-1 2)]
    (testing "functionality of scalar-mod"
      (is (= '(1 1 1) result)))
    (testing "scalar-mod returns collection of type bigint"
      (is (= true (instance? BigInt (first result))))
      (is (= true (instance? BigInt (last result)))))))

(deftest scalar-mul-test
  (let [result (scalar-mul big-coll-1 3)]
    (testing "functionality of scalar-mul"
      (is (= '(9 9 9) result)))
    (testing "scalar-mul returns collection of type bigint"
      (is (= true (instance? BigInt (first result))))
      (is (= true (instance? BigInt (last result)))))))

(deftest elementwise-min-test
  (let [result (elementwise-min big-coll-1 big-coll-2)]
    (testing "functionality of elementwise-min"
      (is (= '(3 3 3) result)))
    (testing "elementwise-min returns collection of type bigint"
      (is (= true (instance? BigInt (first result))))
      (is (= true (instance? BigInt (last result)))))))

(deftest elementwise-add-test
  (let [result (elementwise-add big-coll-1 big-coll-2)]
    (testing "functionality of elementwise-add"
      (is (= '(13 13 13) result)))
    (testing "elementwise-add returns collection of type bigint"
      (is (= true (instance? BigInt (first result))))
      (is (= true (instance? BigInt (last result)))))))

(deftest dot-test
  (testing "dot product between two vectors of equal length"
    (is (= 70 (dot [1 2 3 4 ] [5 6 7 8])))))

(deftest hamming-distance-test
  (testing "hamming-distance, number of differing elements between two collections"
    (is (= 3 (hamming-distance [2 1 7 3 8 9 6] [2 2 3 3 7 9 6])))
    (is (= 2 (hamming-distance [1 0 1 1 1 0 1] [1 0 0 1 0 0 1])))
    (is (= 0 (hamming-distance [1 2 3 4 5 6 7] [1 2 3 4 5 6 7])))))

(deftest cosine-distance-test
  (testing "cosine distance between two different vectors"
    (is (= 9/100 (/ (Math/round ^float (* 100 (cosine-distance [1 2 3 4 5] [1 2 3 6 7]))) 100))))
  (testing "cosine distance between two equal vectors"
    (is (= 0.0 (cosine-distance [1 2 3 4 5] [1 2 3 4 5])))))

(deftest jaccard-test
  (testing "jaccard functionality"
    (is (= 0 (jaccard-similarity big-coll-1 big-coll-2))
        (= 2/3 (jaccard-similarity big-coll-1 big-coll-3)))))