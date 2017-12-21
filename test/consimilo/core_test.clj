(ns consimilo.core-test
  (:require [clojure.test :refer :all]
            [consimilo.core :refer :all]))

(def minhash1 {:label "1" :vector ["1" "2" "3"]})
(def minhash2 {:label "2" :vector ["1" "2" "10"]})
(def minhash3 {:label "3" :vector ["32" "64" "128"]})

(deftest core-add-all-test
  (testing "core add all returnes indexed forest"
    (is (> (count (get-in @(add-all-to-forest [minhash1 minhash2 minhash3])
                          [:sorted-hash :0]))
           0))))

(deftest core-query-test
  (testing "query api returns best results"
    (is (= '("1" "2")
           (query-forest (add-all-to-forest [minhash1 minhash2 minhash3])
                         ["1" "2" "4"]
                         2)))))

(deftest core-add-strings-test
  (testing "adding several strings to forest"
    (is (> (count (get-in @(add-strings-to-forest [{:label "1" :vector "my awesome string"}
                                                   {:label "2" :vector "a string abot cats"}
                                                   {:label "3" :vector "cat's aren't awesome"}])
                          [:sorted-hash :0]))
           0))))
