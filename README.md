# backtick

A Clojure library providing the syntax-quote reader macro as a normal macro.


## Usage

```clojure
(require '[backtick :refer (syntax-quote)])

(let [x 5 v [:a :b]]
  (syntax-quote {:x ~x, 's #{~@v "c"}}))
```

Returns:

```clojure
{(quote s) #{"c" :a :b}, :x 5}
```

Note that symbols are not resolved.
You can provide your own resolver by redefining `backtick/resolve`.

A future version may provide a default resolve implementation,
but I'm this module may be obsoleted as ClojureScript becomes self-hosted.


## License

Copyright © 2012 Brandon Bloom

Distributed under the Eclipse Public License, the same as Clojure.
