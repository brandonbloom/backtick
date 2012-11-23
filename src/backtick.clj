(ns backtick
  (:refer-clojure :exclude [eval resolve]))

(def ^:dynamic *resolve*)

(def ^:dynamic ^:private *gensyms*)

(defn- resolve [sym]
  (let [ns (namespace sym)
        n (name sym)]
    (if (and (not ns) (= (last n) \#))
      (if-not *gensyms*
        (throw (Exception. "Gensym literal not in syntax-quote."))
        (if-let [gs (@*gensyms* sym)]
          gs
          (let [gs (gensym (str (subs n (dec (count n))) "__auto__"))]
            (swap! *gensyms* assoc sym gs)
            gs)))
      (*resolve* sym))))

(defn- unquote? [form]
  (and (seq? form) (= (first form) 'clojure.core/unquote)))

(defn- unquote-splicing? [form]
  (and (seq? form) (= (first form) 'clojure.core/unquote-splicing)))

(defn quote-fn [form]
  (cond
    (symbol? form) `'~(resolve form)
    (unquote? form) (second form)
    (unquote-splicing? form) (throw (Exception. "splice not in list"))
    (coll? form)
      (let [xs (if (map? form) (apply concat form) form)
            parts (for [x (partition-by unquote-splicing? xs)]
                    (if (unquote-splicing? (first x))
                      (second (first x))
                      (mapv quote-fn x)))
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
       (binding [*resolve* resolver#
                 *gensyms* (atom {})]
         (quote-fn form#)))))

(defquote template identity)
