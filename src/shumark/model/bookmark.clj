(ns shumark.model.bookmark
  (:require [clojure.java.jdbc :as jdbc]
            [shumark.model.db :as db]))

(defn select []
  (prn "db-url:" db/db-url)
  (jdbc/with-connection db/db-url
    (jdbc/with-query-results res ["select * from bookmark"] (vec res))))

(defn insert [m]
  (jdbc/with-connection db/db-url
    (jdbc/update-or-insert-values :bookmark
                                  ["url=?" (:url m)]
                                  m)))

(defn update [m] (insert m))

(defn delete [bookmark-id]
  (jdbc/with-connection db/db-url
    (jdbc/delete-rows :bookmark
                      ["bookmark_id=?" bookmark-id])))

