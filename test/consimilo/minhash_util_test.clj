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

(deftest scalar-and-test
  (let [result (scalar-and big-coll-1 2)]
    (testing "functionality of scalar-and"
      (is (= '(2 2 2) result)))
    (testing "scalar-and returns collection of type bigint"
      (is (= true (instance? BigInt (first result))))
      (is (= true (instance? BigInt (last result)))))))

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

(deftest jaccard-test
  (testing "jaccard functionality"
    (is (= 0 (jaccard big-coll-1 big-coll-2))
        (= 2/3 (jaccard big-coll-1 big-coll-3)))))