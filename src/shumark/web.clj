(ns shumark.web
  (:use [ring.adapter.jetty :only [run-jetty]]))

(defn app [req]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body "this is shumark"})

(defn -main [& m]
  (run-jetty app {:join? false
                  :port (Integer. (or (System/getenv "PORT") (:port m) "8080"))}))

