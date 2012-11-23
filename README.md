# backtick

A Clojure library providing the syntax-quote reader macro as a normal macro.


## Motivation

Clojure's backtick `` ` `` reader macro, called syntax-quote, complects the
templating of Clojure forms with Clojure's namespaced symbol resolution.

Backtick allows you to use the unquote `` ~ `` and unquote-splicing `` ~@ ``
metacharacters for templating forms with or without a customer symbol resolver.

Lots more background at <http://blog.brandonbloom.name/2012/11/templating-clojures-backtick.html>.

## Usage

```clojure
(use 'backtick)

(let [x 5 v [:a :b]]
  (template {:x ~x, s #{~@v "c"}}))

;; Returns:
{s #{"c" :a :b}, :x 5}
```

Note that symbols are not resolved. However, gensyms are supported:

```clojure
(template [x# x# y#])

;; Returns something like:
[x__auto__990 x__auto__990 y__auto__991]
```

You can create a templating macro with your own resolver with defquote:

```clojure
(defquote shout-quote (comp symbol clojure.string/upper-case))

(shout-quote {:foo bar})

;; Returns:
{:foo BAR}
```


## License

Copyright © 2012 Brandon Bloom

Distributed under the Eclipse Public License, the same as Clojure.
