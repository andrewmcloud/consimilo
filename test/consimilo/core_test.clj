(ns consimilo.core-test
  (:require [clojure.test :refer :all]
            [consimilo.core :refer :all]
            [clojure.java.io :as io]))

;; TODO: add more extensive testing of core

(def minhash1 {:id "1" :features ["1" "2" "3"]})
(def minhash2 {:id "2" :features ["1" "3" "10"]})
(def minhash3 {:id "3" :features ["32" "64" "128"]})

(def forest-from-hash (add-all-to-forest [minhash1 minhash2 minhash3]))
(def forest-from-strings (add-strings-to-forest [{:id "1" :features "My name is Andrew and I live in Charleston SC. I am staying home for Christmas this year."}
                                                 {:id "2" :features "My name is Christina and I live in West Ashley SC. I am not staying home for Christmas this year."}
                                                 {:id "3" :features "My name is David and I live in Summerville, SC. I am going to go to Florida for Christmas this year."}]))

(deftest core-add-all-test
  (testing "core add all returns indexed forest"
    (is (> (count (get-in @forest-from-hash
                          [:sorted-hash :0]))
           0))))

(deftest core-query-test
  (testing "query api returns best results"
    (is (= '(:1 :2)
           (:top-k (query-forest forest-from-hash
                                 ["1" "2" "4"]
                                 2)))))
  (testing "query-string"
    (is (= '(:1 :2)
           (:top-k (query-string forest-from-strings
                                 "My name is Bonnie and I live in Charleston, SC. I am staying home for Christmas this year."
                                 2)))))

  (testing "query-file"
    (is (= '(:1 :2)
           (:top-k (query-file forest-from-strings
                               (io/resource "test.txt")
                               2))))))

(deftest core-add-strings-test
  (testing "adding several strings to forest"
    (is (> (count (get-in @forest-from-strings
                          [:sorted-hash :0]))
           0))))

(deftest core-jaccard-k
  (testing "calculate jaccard on top-k results, string input"
    (is (>= (:1 (jaccard-k forest-from-strings
                           "My name is Bonnie and I live in Charleston, SC. I am staying home for Christmas this year."
                           1))
            3/4)))
  (testing "calculate jaccard on top-k results, file input"
    (is (>= (:1 (jaccard-k forest-from-strings
                           (io/resource "test.txt")
                           1))
            3/4))))