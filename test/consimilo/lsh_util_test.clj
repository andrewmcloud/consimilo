(ns consimilo.lsh-util-test
  (:require [clojure.test :refer :all]
            [consimilo.lsh-util :refer :all]))

(deftest get-hashranges-test
  (testing "all ranges less than k * num trees"
    (is (every? #(>= (* 5 8) %) (map #(apply max %) (get-hashranges 5 8)))))
  (testing "buckets are the same size"
    (is (apply = (map #(- (second %) (first %)) (get-hashranges 5 8))))))

(deftest get-range-test
  (testing "returns correct number of buckets"
    (is (= 40 (get-range 5 8)))))

(deftest keyword-int-test
  (testing "returns int turned into keyword"
    (is (= :0 (keyword-int 0)))))

(deftest build-hashtables-test
  (testing "initializes empty hashtables structure"
    (is (= {:0 {} :1 {} :2 {}}
           (build-hashtables 3)))))

(deftest build-sorted-hashtables-test
  (testing "initializes empty sorted-hashtables structure"
    (is (= {:0 [] :1 [] :2 []}
           (build-sorted-hashtables 3)))))

(deftest slice-test
  (let [private-slice #'consimilo.lsh-util/slice]
    (testing "slice of empty coll"
      (is (= ()
             (private-slice 0 10 []))))
    (testing "slice at begining"
      (is (= '(1 2 3)
             (private-slice 0 3 [1 2 3 4 5]))))
    (testing "slice in middle"
      (is (= '(2 3 4)
             (private-slice 1 4 [1 2 3 4 5]))))))

(deftest slice-minhash-test
  (testing "returns sequence of slices"
    (is (= '((1 2 3) (4 5 6))
           (slice-minhash [1 2 3 4 5 6] [[0 3] [3 6]])))))

(deftest tree-keys-test
  (testing "correct keywords"
    (is (= [:0 :1 :2]
           (tree-keys 3)))))

(deftest func-search-test
  (testing "search for min"
    (is (= 0
           (pred-search #(>= % 5) 10)))))
