(defproject consimilo "0.1.1"
  :description "A Clojure library for querying large data-sets on similarity"
  :url "http://github.com/andrewmcloud/consimilo"
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [yogthos/config "1.1.1"]
                 [clojure-opennlp "0.4.0"]
                 [com.novemberain/pantomime "2.10.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [com.taoensso/nippy "2.13.0"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
