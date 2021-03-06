(ns shumark.model.user
  (:require [clojure.java.jdbc :as jdbc]
            [shumark.model.db :as db]))

(defn select-by-email [email]
  (db/with-db
    (jdbc/with-query-results res ["select * from bookmark_user where email = ?" email] (first res))))

(defn insert [email]
  (db/with-db
    (jdbc/insert-records :bookmark_user {:email email})))

(defn insert-if-not-exist
  "select the record from table by email,
   insert a new record if the email does not exist."
  [email]
  (db/with-db
    (jdbc/transaction
     (if-let [user (select-by-email email)]
       user
       (first (insert email))))))
