(ns backtick
  (:refer-clojure :exclude [resolve]))

(def ^:dynamic *resolve*)

(def ^:dynamic ^:private *gensyms*)

(defn error [msg form]
  (throw (ex-info msg {:form form})))

(defn- resolve [sym]
  (let [ns (namespace sym)
        n (name sym)]
    (if (and (not ns) (= (last n) \#))
      (if-let [gs (@*gensyms* sym)]
        gs
        (let [gs (gensym (str (subs n 0 (dec (count n))) "__auto__"))]
          (swap! *gensyms* assoc sym gs)
          gs))
      (*resolve* sym))))

(defn unquote? [form]
  (and (seq? form) (= (first form) 'clojure.core/unquote)))

(defn unquote-splicing? [form]
  (and (seq? form) (= (first form) 'clojure.core/unquote-splicing)))

(defn inert? [x]
  (or (nil? x)
      (keyword? x)
      (number? x)
      (string? x)
      (instance? Boolean x)
      (= () x)))

(declare quote-fn*)

(defn splice-items [coll]
  (let [xs (if (map? coll) (apply concat coll) coll)
        parts (for [x xs]
                (if (unquote-splicing? x)
                  (second x)
                  [(quote-fn* x)]))
        cat (doall `(concat ~@parts))]
    (cond
      (vector? coll) `(vec ~cat)
      (map? coll) `(apply hash-map ~cat)
      (set? coll) `(set ~cat)
      (seq? coll) `(apply list ~cat)
      :else (error "Unknown collection type" coll))))

(defn quote-items [coll]
  ((cond
     (vector? coll) vec
     (map? coll) #(into {} %)
     (set? coll) set
     (seq? coll) #(list* 'list (doall %))
     :else (error "Unknown collection type" coll))
   (map quote-fn* coll)))

(defn- quote-fn* [form]
  (cond
    (inert? form) form
    (symbol? form) `'~(resolve form)
    (unquote? form) (second form)
    (unquote-splicing? form) (error "splice not in collection" form)
    (record? form) `'~form
    (coll? form) (if (some unquote-splicing? form)
                   (splice-items form)
                   (quote-items form))
    :else `'~form))

(defn quote-fn [resolver form]
  (binding [*resolve* resolver
            *gensyms* (atom {})]
    (quote-fn* form)))

(defmacro defquote [name resolver]
  `(let [resolver# ~resolver]
     (defn ~(symbol (str name "-fn")) [form#]
       (quote-fn resolver# form#))
     (defmacro ~name [form#]
       (quote-fn resolver# form#))))

(defquote template identity)

(defn- class-symbol [^java.lang.Class cls]
  (symbol (.getName cls)))

(defn- namespace-name [^clojure.lang.Namespace ns]
  (name (.getName ns)))

(defn- var-namespace [^clojure.lang.Var v]
  (name (.name (.ns v))))

(defn- var-name [^clojure.lang.Var v]
  (name (.sym v)))

(defn- var-symbol [^clojure.lang.Var v]
  (symbol (var-namespace v) (var-name v)))

(defn- ns-resolve-sym [sym]
  (try
    (let [x (ns-resolve *ns* sym)]
      (cond
        (instance? java.lang.Class x) (class-symbol x)
        (instance? clojure.lang.Var x) (var-symbol x)
        :else nil))
    (catch ClassNotFoundException _
      sym)))

(defn resolve-symbol [sym]
  (let [ns (namespace sym)
        nm (name sym)]
    (if (nil? ns)
      (if-let [[_ ctor-name] (re-find #"(.+)\.$" nm)]
        (symbol nil (-> (symbol nil ctor-name)
                      resolve-symbol
                      name
                      (str ".")))
        (if (or (special-symbol? sym)
                (re-find #"^\." nm)) ; method name
          sym
          (or (ns-resolve-sym sym)
              (symbol (namespace-name *ns*) nm))))
      (or (ns-resolve-sym sym) sym))))

(defquote syntax-quote resolve-symbol)
