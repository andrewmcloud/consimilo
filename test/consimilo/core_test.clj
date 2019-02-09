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
                                                 {:id "3" :features "My name is David and I reside in Summerville, SC. I am going to go Florida for Christmas this year."}]))
(def forest-from-one-file (add-files-to-forest [(io/as-file (io/resource "test.txt"))]))
(def forest-from-files (add-files-to-forest [(io/as-file (io/resource "test1.txt"))] :forest forest-from-one-file))

(deftest core-add-all-test
  (testing "core add all returns indexed forest"
    (is (> (count (get-in @forest-from-hash
                          [:sorted-hash :0]))
           0))))

(deftest core-query-test
  (testing "query api returns best results"
    (is (= '(:1 :2)
           (:top-k (query-forest forest-from-hash
                                 2
                                 ["1" "2" "4"])))))
  (testing "query-string"
    (is (= '(:1 :2)
           (:top-k (query-string forest-from-strings
                                 2
                                 "My name is Bonnie and I live in Charleston SC. I am staying home for Christmas this year.")))))

  (testing "query-string - forest built form files incrementally"
    (is (= '(:test1.txt)
           (:top-k (query-string forest-from-files
                                 1
                                 "My name is Andrew and I am the author of this codebase. I live in Charleston, SC.")))))

  (testing "query-file"
    (is (= '(:1 :2)
           (:top-k (query-file forest-from-strings
                               2
                               (io/as-file (io/resource "test.txt"))))))))


(deftest core-add-strings-test
  (testing "adding several strings to forest"
    (is (> (count (get-in @forest-from-strings
                          [:sorted-hash :0]))
           0))))

(deftest core-jaccard-k
  (testing "calculate jaccard on top-k results, string input"
    (is (>= (:1 (similarity-k forest-from-strings
                              1
                              "My name is Bonnie and I live in Charleston SC. I am staying home for Christmas this year."
                              :sim-fn :jaccard))
            3/5)))
  (testing "calculate jaccard on top-k results, file input"
    (is (>= (:1 (similarity-k forest-from-strings
                              1
                              (io/resource "test.txt")
                              :sim-fn :jaccard))
            3/5)))
  (testing "calculate jaccard on top-k results, feature-vector input"
    (is (>= (:1 (similarity-k forest-from-hash
                              1
                              ["1" "2" "3"]
                              :sim-fn :jaccard))
            1))))

(deftest core-cosine
  (testing "calculate cosine distance on top-k results with a string input"
    (is (= (:1 (similarity-k forest-from-strings
                             1
                             "My name is Andrew and I live in Charleston SC. I am staying home for Christmas this year."
                             :sim-fn :cosine))
           0.0)))
  (testing "calculate cosine distance on top-k results with a file input"
    (is (>= (:1 (similarity-k forest-from-strings
                              1
                              (io/as-file (io/resource "test.txt"))
                              :sim-fn :cosine))
            19.91)))
  (testing "calculate cosine distance on top-k results with a feature-vector input"
    (is (>= (:2 (similarity-k forest-from-hash
                              1
                              ["1" "3" "128"]
                              :sim-fn :cosine))
            34.97))))

(deftest core-hamming
  (testing "calculate cosine distance on top-k results with a string input"
    (is (= (:1 (similarity-k forest-from-strings
                             1
                             "My name is Anabelle and I live in Charleston SC. I am staying home for Christmas this year."
                             :sim-fn :hamming))
           27)))
  (testing "calculate cosine distance on top-k results with a file input"
    (is (= (:1 (similarity-k forest-from-strings
                             1
                             (io/as-file (io/resource "test.txt"))
                             :sim-fn :hamming))
           26)))
  (testing "calculate cosine distance on top-k results with a feature-vector input"
    (is (= (:2 (similarity-k forest-from-hash
                             1
                             ["1" "3" "128"]
                             :sim-fn :hamming))
           66))))

(deftest serialize-test
  (testing "save forest to file, load forest, query"
    (let [loaded-forest (thaw-forest (io/resource "testforest"))]
      (is (= '(:2 :3)
             (:top-k (query-string loaded-forest
                                   2
                                   "My name is Bonnie and I live in Charleston SC. I am staying home for Christmas this year.")))))))