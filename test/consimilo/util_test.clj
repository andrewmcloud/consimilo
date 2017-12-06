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
  (testing "scalar and"
    (let [result (scalar-and big-coll-1 2)]
      (is (= '(2 2 2) result))
      (is (= true (instance? BigInteger (first result)))))))

(deftest scalar-mod-test
  (testing "scalar mod"
    (let [result (scalar-mod big-coll-1 2)]
      (is (= '(1 1 1) result))
      (is (= true (instance? BigInteger (first result)))))))

(deftest scalar-mul-test
  (testing "scalar mul"
    (let [result (scalar-mul big-coll-1 3)]
      (is (= '(9 9 9) result))
      (is (= true (instance? BigInteger (first result)))))))

(deftest elementwise-min-test
  (testing "elementwise min"
    (let [result (elementwise-min big-coll-1 big-coll-2)]
      (is (= '(3 3 3) result))
      (is (= true (instance? BigInteger (first result)))))))

(deftest elementwise-add-test
  (testing "elementwise add"
    (let [result (elementwise-add big-coll-1 big-coll-2)]
      (is (= '(13 13 13) result))
      (is (= true (instance? BigInteger (first result)))))))

(deftest count-equal-test
  (testing "count-equal"
    (let [private-count-equal #'consimilo.util/count-equal]
      (is (= 2 (private-count-equal big-coll-1 big-coll-3))))))

(deftest jaccard-test
  (testing "jaccard"
    (is (= 0 (jaccard big-coll-1 big-coll-2))
        (= 2/3 (jaccard big-coll-1 big-coll-3)))))