(ns shumark.util.test.err
  (:use clojure.test
        shumark.util.err))

(deftest with-default-test
  (is (= 2 (with-dflt 2 (/ 2 1)))))