(ns backtick-test
  (:use clojure.test)
  (:require [backtick :refer (template defquote)]))

(deftest template-test

  (testing "Primitives, collections, unquote, and splice; symbols qualified"
    (let [n 5 v [:a :b]]
      (is (=           `(5 nil true a/b ~n [p/q ~@v r/s] {:x #{"s"}})
              (template (5 nil true a/b ~n [p/q ~@v r/s] {:x #{"s"}}))))))

  (testing "Automatic gensyms"
    (let [[a b c d] (template [foo# bar# foo# bar])]
      (is (not= a 'foo))
      (is (not= a 'foo#))
      (is (= a c))
      (is (not= a b))
      (is (= d 'bar)))))

(defquote wacky-quote (fn [sym] (symbol (str sym "!"))))

(deftest defquote-test
  (testing "Custom resolver"
    (is (= '(foo! :a [5 bar!])
           (wacky-quote (foo :a [5 bar]))))))
