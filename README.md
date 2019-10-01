# backtick

A Clojure library providing the syntax-quote (aka quasiquote) reader macro as a normal macro.


## Motivation

Clojure's backtick `` ` `` reader macro, called syntax-quote, complects the
templating of Clojure forms with Clojure's namespaced symbol resolution.

Backtick allows you to use the unquote `` ~ `` and unquote-splicing `` ~@ ``
metacharacters for templating forms with or without custom symbol qualifiers and expanders.

Lots more background at <http://blog.brandonbloom.name/2012/11/templating-clojures-backtick.html>.


## Installation

Artifacts are hosted on Clojars: <https://clojars.org/backtick>


## Usage

```clojure
(use 'backtick)

;; Full syntax-quote replacement
(let [x 5 v [:a :b]]
  (syntax-quote {:x ~x, s #{~@v "c" inc}}))

;; Prints at the REPL:
{:x 5, user/s #{"c" clojure.core/inc :a :b}}

;; Templating only, no symbol resolution
(let [x 5 v [:a :b]]
  (template {:x ~x, s #{~@v "c" inc}}))

;; Prints at the REPL:
{:x 5, s #{"c" :a :b inc}}

;; Templating with a custom expander
(template '{a "ABC" z ["XYZ"]}  [~a ~@z])

;; Prints at the REPL:
["ABC" "XYZ"]
```

Note that while `template` does not qualify symbols, it does support gensyms:

```clojure
(template [x# x# y#])

;; Returns something like:
[x__auto__990 x__auto__990 y__auto__991]
```

You can create a templating macro with a custom qualifier by using `defquote`:

```clojure
(defquote shout-quote (comp symbol clojure.string/upper-case))

(shout-quote {:foo bar})

;; Prints at the REPL:
{:foo BAR}
```

You can also define a templating macro with a custom expander, which is run only
on expanded expressions (and conversely a qualifier only runs on unexpanded expressions).

It can be used to let the expanded expressions refer to something else than the
lexical scope, so as to fill the placeholders with the content of a map for
instance.

```clojure
(defquote abbrev-quote identity '{b 'bar})

(abbrev-quote {:foo ~b})

;; Prints at the REPL:
{:foo bar}
```

It is also possible to override the expander of a templating macro (by default,
`identity`) by specifying it at the call site.

```clojure
(def abbreviations
  '{b 'bar})

(template abbreviations {:foo ~b})

;; Prints at the REPL:
{:foo bar}
```

Corresponding functions are generated for every quoting macro:

```clojure
(syntax-quote-fn 'foo) ;; => (quote user/foo)
(template-fn 'foo)     ;; => (quote foo)
(shout-quote-fn 'foo)  ;; => (quote FOO)
(abbrev-quote-fn '~b)  ;; => (quote bar)
```


## License

Copyright © 2012 Brandon Bloom

Distributed under the Eclipse Public License, the same as Clojure.
