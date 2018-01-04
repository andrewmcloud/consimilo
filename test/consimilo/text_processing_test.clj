(ns consimilo.text-processing-test
  (:require [clojure.test :refer :all]
            [consimilo.text-processing :refer :all]
            [clojure.java.io :as io]))

(deftest remove-stopwords-test
  (let [private-remove-stopwords #'consimilo.text-processing/remove-stopwords]
    (testing "remove-stopwords, remove-stopwords? true"
      (is (= ["name" "andrew" "live" "charleston"]
             (private-remove-stopwords true ["my" "name" "is" "andrew" "and" "i" "live" "in" "charleston"]))))
    (testing "remove-stopwords, remove-stopwords? false"
      (is (= ["my" "name" "is" "andrew" "and" "i" "live" "in" "charleston"]
             (private-remove-stopwords false ["my" "name" "is" "andrew" "and" "i" "live" "in" "charleston"]))))))

(deftest tokenize-text-test
  (testing "tokenize text, remove-stopwords? true"
    (is (= ["name" "andrew" "live" "charleston"]
           (tokenize-text "My name is Andrew and I live in Charleston"))))
  (testing "tokenize text, remove-stopwords? false"
    (is (= ["my" "name" "is" "andrew" "and" "i" "live" "in" "charleston"]
           (tokenize-text "My name is Andrew and I live in Charleston"
                          :remove-stopwords? false)))))

(deftest shingle-test
  (testing "shingle, n 3"
    (is (= ["mynameis" "nameisandrew" "isandrewand" "andrewandi" "andilive" "ilivein" "liveincharleston"]
           (shingle (tokenize-text "My name is Andrew and I live in Charleston"
                                   :remove-stopwords? false)
                    3))))
  (testing "shingle, n = 1"
    (is (= ["my" "name" "is" "andrew" "and" "i" "live" "in" "charleston"]
           (shingle (tokenize-text "My name is Andrew and I live in Charleston"
                                   :remove-stopwords? false)
                    1))))
  (testing "shingle, n < 1"
    (is (= ["my" "name" "is" "andrew" "and" "i" "live" "in" "charleston"]
           (shingle (tokenize-text "My name is Andrew and I live in Charleston"
                                   :remove-stopwords? false)
                    -1))))
  (testing "shingle, n > (count tokenize-text)"
    (is (= ["my" "name" "is" "andrew" "and" "i" "live" "in" "charleston"]
           (shingle (tokenize-text "My name is Andrew and I live in Charleston"
                                   :remove-stopwords? false)
                    20)))))

(deftest extract-text-test
  (testing "extracting text from file"
    (is (= "My name is Bonnie and I live in Charleston SC. I am staying home for Christmas this year.\n"
           (extract-text (io/resource "test.txt"))))))