# backtick

A Clojure library providing the syntax-quote (aka quasiquote) reader macro as a normal macro.


## Motivation

Clojure's backtick `` ` `` reader macro, called syntax-quote, complects the
templating of Clojure forms with Clojure's namespaced symbol resolution.

Backtick allows you to use the unquote `` ~ `` and unquote-splicing `` ~@ ``
metacharacters for templating forms with or without a custom symbol resolver.

Lots more background at <http://blog.brandonbloom.name/2012/11/templating-clojures-backtick.html>.


## Installation

Artifacts are hosted on Clojars: <https://clojars.org/backtick>


## Usage

```clojure
(use 'backtick)

;; Full syntax-quote replacement
(let [x 5 v [:a :b]]
  (syntax-quote {:x ~x, s #{~@v "c" inc}}))

;; Returns:
{:x 5, user/s #{"c" clojure.core/inc :a :b}}

;; Templating only, no symbol resolution
(let [x 5 v [:a :b]]
  (template {:x ~x, s #{~@v "c" inc}}))

;; Returns:
{s #{"c" :a :b inc}, :x 5}
```

Note that while `template` does not resolve symbols, it does support gensyms:

```clojure
(template [x# x# y#])

;; Returns something like:
[x__auto__990 x__auto__990 y__auto__991]
```

You can create a templating macro with a custom resolver by using `defquote`:

```clojure
(defquote shout-quote (comp symbol clojure.string/upper-case))

(shout-quote {:foo bar})

;; Returns:
{:foo BAR}
```

Corresponding functions are generated for every quoting macro:

```clojure
(syntax-quote-fn 'foo) ;; => (quote user/foo)
(template-fn 'foo)     ;; => (quote foo)
(shout-quote-fn 'foo)  ;; => (quote FOO)
```


## License

Copyright © 2012 Brandon Bloom

Distributed under the Eclipse Public License, the same as Clojure.
