(ns shumark.model.user
  (:require [clojure.java.jdbc :as jdbc]
            [shumark.model.db :as db]))

(defn select-by-email [email]
  (jdbc/with-connection db/db-url
    (jdbc/with-query-results res ["select * from bookmark_user where email = ?" email] (vec res))))

(defn insert [email]
  (jdbc/with-connection db/db-url
    (jdbc/insert-values :bookmark_user [:email email])))