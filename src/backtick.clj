(ns backtick
  (:refer-clojure :exclude [eval resolve]))

(defn resolve [sym] sym)

(defn- unquote? [form]
  (and (seq? form) (= (first form) 'clojure.core/unquote)))

(defn- unquote-splicing? [form]
  (and (seq? form) (= (first form) 'clojure.core/unquote-splicing)))

(defn syntax-quote-fn [form]
  (cond
    (symbol? form) `'~(resolve form)
    (unquote? form) (second form)
    (unquote-splicing? form) (throw (Exception. "splice not in list"))
    (coll? form)
      (let [xs (if (map? form) (apply concat form) form)
            cat `(concat ~@(for [x (partition-by unquote-splicing? xs)]
                             (if (unquote-splicing? (first x))
                               (second (first x))
                               `[~@(map syntax-quote-fn x)])))]
        (cond
          (vector? form) `(vec ~cat)
          (map? form) `(apply hash-map ~cat)
          (set? form) `(set ~cat)
          (seq? form) `(list* ~cat)
          :else (throw (Exception. "Unknown collection type"))))
    :else `'~form))

(defmacro syntax-quote [form]
  (syntax-quote-fn form))
