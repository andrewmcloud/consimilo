(ns consimilo.util-test
  (:require [clojure.test :refer :all]
            [consimilo.util :refer :all]))

(defn biginteger_vec
  [n val]
  (repeat n (biginteger val)))

(def big-coll-1 (biginteger_vec 3 3))
(def big-coll-2 (biginteger_vec 3 10))
(def big-coll-3 [(biginteger 3) (biginteger 3) (biginteger 2)])

(deftest scalar-and-test
  (let [result (scalar-and big-coll-1 2)]
    (testing "functionality of scalar-and"
      (is (= '(2 2 2) result)))
    (testing "scalar-and returns collection of type BigInteger"
      (is (= true (instance? BigInteger (first result))))
      (is (= true (instance? BigInteger (last result)))))))

(deftest scalar-mod-test
  (let [result (scalar-mod big-coll-1 2)]
    (testing "functionality of scalar-mod"
      (is (= '(1 1 1) result)))
    (testing "scalar-mod returns collection of type BigInteger"
      (is (= true (instance? BigInteger (first result))))
      (is (= true (instance? BigInteger (last result)))))))

(deftest scalar-mul-test
  (let [result (scalar-mul big-coll-1 3)]
    (testing "functionality of scalar-mul"
      (is (= '(9 9 9) result)))
    (testing "scalar-mul returns collection of type BigInteger"
      (is (= true (instance? BigInteger (first result))))
      (is (= true (instance? BigInteger (last result)))))))

(deftest elementwise-min-test
  (let [result (elementwise-min big-coll-1 big-coll-2)]
    (testing "functionality of elementwise-min"
      (is (= '(3 3 3) result)))
    (testing "elementwise-min returns collection of type BigInteger"
      (is (= true (instance? BigInteger (first result))))
      (is (= true (instance? BigInteger (last result)))))))

(deftest elementwise-add-test
  (let [result (elementwise-add big-coll-1 big-coll-2)]
    (testing "functionality of elementwise-add"
      (is (= '(13 13 13) result)))
    (testing "elementwise-add returns collection of type BigInteger"
      (is (= true (instance? BigInteger (first result))))
      (is (= true (instance? BigInteger (last result)))))))

(deftest intersection-ct-test
  (let [private-intersection-ct #'consimilo.util/intersection-ct]
    (testing "intersection-ct functionality"
      (is (= 2 (private-intersection-ct big-coll-1 big-coll-3))))))

(deftest jaccard-test
  (testing "jaccard functionality"
    (is (= 0 (jaccard big-coll-1 big-coll-2))
        (= 2/3 (jaccard big-coll-1 big-coll-3)))))