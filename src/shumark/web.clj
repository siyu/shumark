(ns shumark.web
  (:use [ring.adapter.jetty :only [run-jetty]])
  (:require [shumark.app :as app]))

(defn app [req]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "Hello, world"})

(defn -main [& m]
  (println "alan....")
  (run-jetty #'app {:join? false :port (Integer. (or (System/getenv "PORT") (:port m) "8080"))}))

(comment
  (-main))