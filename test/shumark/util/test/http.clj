(ns shumark.util.test.http
  (:use clojure.test
        shumark.util.http))

(deftest transform-url-test
  (are [orig-url new-url] (= (transform-url orig-url) new-url)
       "http://www.yahoo.com" "http://www.yahoo.com"
       "https://q" "https://q"
       "ftp://x" "ftp://x"
       "x" "http://x"
       " ftp://" ""
       "" "http://"
       "://" ""
       ":this-is-bad" ""))
