(ns consimilo.random-seed-test
  (:refer-clojure :exclude [rand rand-biginteger])
  (:require [clojure.test :refer :all]
            [consimilo.random-seed :refer :all]))

(defn- get-seeded-random
  "Generates a seeded random number for testing"
  [seed max-range]
  (set-random-seed! seed)
  (rand-biginteger max-range))

(defn- get-seeded-random-vec
  "Generates a seeded random vector for testing"
  [seed n max-range]
  (set-random-seed! seed)
  (rand-vec n max-range))

(deftest rand-biginteger-test
  (testing "ensure seeded rand-biginteger returns the same value after seeding"
    (is (= (get-seeded-random 3 1024)
           (get-seeded-random 3 1024))))
  (testing "testing rand-biginteger returns type BigInteger"
    (is (= true (instance? BigInteger (rand-biginteger 5))))))

(deftest rand-vec-test
  (testing "seeded rand-vec returns the same random collection each time"
    (is (= (doall (get-seeded-random-vec 1 10 4096))
           (doall (get-seeded-random-vec 1 10 4096)))))
  (testing "rand-vec returns a collection of type BigInteger"
    (is (= true (instance? BigInteger (first (rand-vec 4 1024)))))
    (is (= true (instance? BigInteger (last (rand-vec 4 1024)))))))
