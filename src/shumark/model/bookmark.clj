(ns shumark.model.bookmark
  (use [environ.core])
  (:require [clojure.java.jdbc :as jdbc]))

(def db-url (env :database-url))

(defn select []
  (prn "here:" db-url)
  (jdbc/with-connection db-url
    (jdbc/with-query-results res ["select * from bookmark"] (vec res))))

(defn insert [m]
  (jdbc/with-connection db-url
    (jdbc/update-or-insert-values :bookmark
                                  ["url=?" (:url m)]
                                  m)))

(defn update [m] (insert m))

(defn delete [m]
  (jdbc/with-connection db-url
    (jdbc/delete-rows :bookmark
                      ["bookmark_id=?" (:bookmark_id m)])))

