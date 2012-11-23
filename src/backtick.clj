(ns backtick
  (:refer-clojure :exclude [eval resolve]))

(def ^:dynamic *resolve*)

(defn- unquote? [form]
  (and (seq? form) (= (first form) 'clojure.core/unquote)))

(defn- unquote-splicing? [form]
  (and (seq? form) (= (first form) 'clojure.core/unquote-splicing)))

(defn syntax-quote-fn [form]
  (cond
    (symbol? form) `'~(*resolve* form)
    (unquote? form) (second form)
    (unquote-splicing? form) (throw (Exception. "splice not in list"))
    (coll? form)
      (let [xs (if (map? form) (apply concat form) form)
            parts (for [x (partition-by unquote-splicing? xs)]
                    (if (unquote-splicing? (first x))
                      (second (first x))
                      (mapv syntax-quote-fn x)))
            cat (doall `(concat ~@parts))]
        (cond
          (vector? form) `(vec ~cat)
          (map? form) `(apply hash-map ~cat)
          (set? form) `(set ~cat)
          (seq? form) `(list* ~cat)
          :else (throw (Exception. "Unknown collection type"))))
    :else `'~form))

(defmacro defquote [name resolver]
  `(let [resolver# ~resolver]
     (defmacro ~name [form#]
       (binding [*resolve* resolver#]
         (syntax-quote-fn form#)))))

(defquote template identity)
