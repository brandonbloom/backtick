(ns backtick-test
  (:use clojure.test)
  (:require [backtick :refer [template defquote syntax-quote quote-fn]])
  (:import clojure.lang.ExceptionInfo))

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
      (is (= d 'bar))))

  (testing "Custom expander"
    (is (= '[:X :Y :Z]       (template '{a :X b :Y c [:Z]} [~a ~b ~@c])))
    (is (= '["ax" "bx" "cx"] (template #(str % \x)         [~a ~b ~c])))
    (let [x 1
          e (try (eval '(backtick/template #(str % x) [~a]))
              (catch Exception e
                e))]
      (is (instance? ExceptionInfo e))
      (is (= (str "Error evaluating templating expander of 'template' "
                  "(always evaluated at macro-expansion time)")
             (.getMessage e))))))

(defn add-bang [sym]
  (symbol (str sym "!")))

(defquote wacky-quote add-bang)

(def the-map
  '{a 'ABBREVIATED})

(defquote abbrev-quote identity the-map)

(deftest defquote-test

  (testing "Custom resolver"
    (is (= ''foo! (quote-fn add-bang 'foo)))
    (is (= ''foo! (wacky-quote-fn 'foo)))
    (is (= '(foo! :a [5 bar!])
           (wacky-quote (foo :a [5 bar])))))

  (testing "Custom expander"
    (is (= ''ABBREVIATED  (quote-fn identity the-map '~a)))
    (is (= ''ABBREVIATED (wacky-quote-fn the-map '~a)))
    (is (= '(ABBREVIATED :a [5 bar!])
           (abbrev-quote (~a :a [5 bar!]))))))

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
