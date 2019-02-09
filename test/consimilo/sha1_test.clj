(ns consimilo.sha1-test
  (:require [clojure.test :refer :all]
            [consimilo.sha1 :refer :all])
  (:import (clojure.lang BigInt)))

(deftest get-hash-bigint-test
  (testing "get-hash-bigint returns type bigint"
    (is (= true (instance? BigInt (get-hash-bigint "andrew"))))))