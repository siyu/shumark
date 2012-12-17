(ns shumark.model.bookmark
  (:require [clojure.java.jdbc :as jdbc]))

(def db {:subprotocol "postgresql"
         :subname "//localhost:5432/shumark"
         :user "siyu"
         :password ""})

(defn select []
  (jdbc/with-connection db
    (jdbc/with-query-results res ["select * from bookmark"] (vec res))))

(defn insert [m]
  (jdbc/with-connection db
    (jdbc/update-or-insert-values :bookmark
                                  ["url=?" (:url m)]
                                  m)))

(defn update [m] (insert m))

(defn delete [m]
  (jdbc/with-connection db
    (jdbc/delete-rows :bookmark
                      ["bookmark_id=?" (:bookmark_id m)])))

