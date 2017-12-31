(ns consimilo.random-seed-test
  (:refer-clojure :exclude [rand rand-bigint])
  (:require [clojure.test :refer :all]
            [consimilo.random-seed :refer :all])
  (:import (clojure.lang BigInt)))

(defn- get-seeded-random
  "Generates a seeded random number for testing"
  [seed max-range]
  (set-random-seed! seed)
  (rand-bigint max-range))

(defn- get-seeded-random-vec
  "Generates a seeded random vector for testing"
  [seed n max-range]
  (set-random-seed! seed)
  (rand-vec n max-range))

(deftest rand-bigint-test
  (testing "ensure seeded rand-bigint returns the same value after seeding"
    (is (= (get-seeded-random 3 1024)
           (get-seeded-random 3 1024))))
  (testing "testing rand-bigint returns type bigint"
    (is (= true (instance? BigInt (rand-bigint 5))))))

(deftest rand-vec-test
  (testing "seeded rand-vec returns the same random collection each time"
    (is (= (doall (get-seeded-random-vec 1 10 4096))
           (doall (get-seeded-random-vec 1 10 4096)))))
  (testing "rand-vec returns a collection of type bigint"
    (is (= true (instance? BigInt (first (rand-vec 4 1024)))))
    (is (= true (instance? BigInt (last (rand-vec 4 1024)))))))