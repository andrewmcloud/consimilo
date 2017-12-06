(ns consimilo.minhash-test
  (:require [clojure.test :refer :all]
            [consimilo.minhash :refer :all]))

(defn build-biginteger-coll
  [coll]
  (map #(biginteger %) coll))

(def minhash-1 (build-biginteger-coll '(1 2 3 4 5 6 7 8 9)))
(def minhash-2 (build-biginteger-coll '(9 8 7 6 5 4 3 2 1)))
(def minhash-3 (build-biginteger-coll '(1 2 3 4 5 4 3 2 1)))

(deftest init-hashvalues-test
  (testing "init-hashvalues"
    (let [private-init-hashvals #'consimilo.minhash/init-hashvalues
          hashvals (private-init-hashvals)]
      (is (= mersenne (first hashvals)))
      (is (= true (instance? BigInteger (first hashvals))))
      (is (= true (instance? BigInteger (last hashvals))))
      (is (= perms (count hashvals))))))

(deftest build-permutations-test
  (testing "build-permutations"
    (let [private-build-permutations #'consimilo.minhash/build-permutations
          private-count-equal #'consimilo.util/count-equal
          p (private-build-permutations)]
      (is (not (nil? (:a p))))
      (is (not (nil? (:b p))))
      (is (= perms (count (:a p))))
      (is (= perms (count (:b p))))
      (is (not= perms (private-count-equal (:a p) (:b p)))))))

(deftest update-minhash-test
  (testing "update-minhash"))

(deftest build-minhash-test
  (testing "build-minhash"
    (let [minhash (build-minhash ["my" "name" "is" "andrew"])]
      (is (= perms (count minhash)))
      (is (= true (instance? BigInteger (first minhash))))
      (is (= true (instance? BigInteger (last minhash)))))))

(deftest merge-minhash-test
  (testing "merge-minhash"
    (is (= minhash-3 (merge-minhash minhash-1 minhash-2)))))