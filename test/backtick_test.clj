(ns backtick-test
  (:use clojure.test)
  (:require [backtick :refer (template defquote quote-fn syntax-quote)]))

(deftest template-test

  (testing "Primitives, collections, unquote, and splice; symbols qualified"
    (let [n 5 v [:a :b]]
      (is (=           `(5 nil () true a/b ~n [p/q ~@v r/s] {:x #{"s"}})
              (template (5 nil () true a/b ~n [p/q ~@v r/s] {:x #{"s"}}))))))

  (testing "Multiple splices"
    (let [v [:a :b] a 5]
      (is (=           `(~a ~@v ~@v ~a)
             (template  (~a ~@v ~@v ~a))))))

  (testing "Automatic gensyms"
    (let [[a b c d] (template [foo# bar# foo# bar])]
      (is (not= a 'foo))
      (is (not= a 'foo#))
      (is (= a c))
      (is (not= a b))
      (is (= d 'bar)))))

(defn add-bang [sym]
  (symbol (str sym "!")))

(defquote wacky-quote add-bang)

(deftest defquote-test
  (testing "Custom resolver"
    (is (= ''foo! (quote-fn add-bang 'foo)))
    (is (= ''foo! (wacky-quote-fn 'foo)))
    (is (= '(foo! :a [5 bar!])
           (wacky-quote (foo :a [5 bar]))))))

(defrecord R [x])

(deftest record-test
  (testing "Record types are preserved"
    (is (= (R. 1) (template #backtick_test.R{:x 1})))))

(deftest syntax-quote-test
  (testing "Constructors, classes, methods, vars, and specials"
    (is (= (syntax-quote
             [Class
              Class.
              java.lang.Class
              java.lang.Class.
              unqualified
              fully/qualified
              .method
              .
              non.existant
              inc
              backtick.test/inc
              quote])
           `[Class
             Class.
             java.lang.Class
             java.lang.Class.
             unqualified
             fully/qualified
             .method
             .
             non.existant
             inc
             backtick.test/inc
             quote]))))
