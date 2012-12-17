(defproject shumark "0.1.0"
  :description "A Bookmarking Web Site"
  :url "http://shumark.herokuapp.com/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [ring "1.1.6"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [postgresql "9.1-901.jdbc4"]
                 [environ "0.3.0"]
                 [clj-time "0.4.4"]
                 [compojure "1.1.3"]
                 [hiccup "1.0.2"]
                 [valip "0.2.0"]])
