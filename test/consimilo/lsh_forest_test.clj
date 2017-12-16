(ns consimilo.lsh-forest-test
  (:require [clojure.test :refer :all]
            [consimilo.lsh-forest :refer :all]
            [consimilo.lsh-util :refer [v=v keyword-int]]
            [consimilo.minhash :refer [build-minhash]]))

(def minhash1 (build-minhash ["1" "2" "3"]))
(def minhash2 (build-minhash ["1" "2" "10"]))
(def minhash3 (build-minhash ["32" "64" "128"]))

(deftest sort-tree-test
  (testing "sort-tree returns empty seq if key not present"
    (is (= {:0 ()}
           (sort-tree {} {} 0))))
  (testing "sort-tree sorts sequence at given key"
    (is (= {:0 '([2 3 1] [3 1 2] [3 2 1])}
           (sort-tree {:hashtables {:0 {'(3 1 2) :a
                                        '(2 3 1) :b
                                        '(3 2 1) :c}}}
                      {}
                      0)))))

(deftest populate-hashtables-test
  (testing "updates might-atom :hashtables entry"
    (with-redefs [mighty-atom (atom {})]
      (let [private-populate-hashtables #'consimilo.lsh-forest/populate-hastables!]
        (private-populate-hashtables "a" minhash1)
        (is (not (empty? (get-in @mighty-atom [:hashtables :0]))))))))

(deftest populate-keys-test
  (testing "updates might-atom :keys entry"
    (with-redefs [mighty-atom (atom {})]
      (let [private-populate-keys #'consimilo.lsh-forest/populate-keys!]
        (private-populate-keys "a" minhash1)
        (is (not (empty? (get-in @mighty-atom [:keys :a]))))))))

(deftest lsh-forest-integration-test
  (testing "lsh forest returns best match on query"
    (dorun
      (map-indexed #(addlsh! (str (inc %)) %2) [minhash1 minhash2 minhash3]))
    (index!)
    (is (= :2 (keyword-int (first (query minhash1 1)))))))
