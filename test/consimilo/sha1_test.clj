(ns consimilo.sha1-test
  (:require [clojure.test :refer :all]
            [consimilo.sha1 :refer :all]))

(deftest get-hash-biginteger-test
  (testing "get-hash-biginteger returns type BigInteger"
    (is (= true (instance? BigInteger (get-hash-biginteger "andrew"))))))

