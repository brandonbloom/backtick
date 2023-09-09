(defproject backtick "0.3.5"
  :description "Provides the syntax-quote reader macro as a normal macro"
  :url "https://github.com/brandonbloom/backtick"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :profiles {:1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}
             :1.10 {:dependencies [[org.clojure/clojure "1.10.3"]]}
             :1.11 {:dependencies [[org.clojure/clojure "1.11.1"]]}}
  :aliases {"test-all" ["with-profile" "dev:dev,1.7:dev,1.8:dev,1.9:dev,1.10:dev,1.11" "test"]})
