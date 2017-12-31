(defproject consimilo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [yogthos/config "0.8"]
                 [clojure-opennlp "0.4.0"]
                 [com.novemberain/pantomime "2.9.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [com.taoensso/nippy "2.13.0"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
