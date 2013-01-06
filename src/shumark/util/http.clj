(ns shumark.util.http
  (:use [ring.util.response :only [response content-type]])
  (:require [clojure.data.json :as json]))

(defn json-resp [x]
  "Create a json response."
  (-> x json/write-str response (content-type "application/json")))

(defn transform-url [url]
  "Prepend http:// if the url does not start with http:// https:// or ftp://"
  (cond (re-find #"^(http|https|ftp)://" url) url
        (re-find #":" url) ""
        :else (str "http://" url)))

