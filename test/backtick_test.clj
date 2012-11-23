(ns backtick-test
  (:use clojure.test)
  (:require [backtick :refer (template defquote)]))

(deftest template-test
  (testing "Primitives, collections, unquote, and splice; symbols qualified"
    (let [n 5 v [:a :b]]
      (is (=           `(5 nil true a/b ~n [p/q ~@v r/s] {:x #{"s"}})
              (template (5 nil true a/b ~n [p/q ~@v r/s] {:x #{"s"}})))))))

(defquote wacky-quote (fn [sym] (symbol (str sym "!"))))

(deftest defquote-test
  (testing "Customer resolver"
    (is (= '(foo! :a [5 bar!])
           (wacky-quote (foo :a [5 bar]))))))
