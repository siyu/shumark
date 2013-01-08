(ns shumark.model.bookmark
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            [shumark.model.db :as db]))

(defn select [user-id]
  (prn "db-url:" db/db-url)
  (jdbc/with-connection db/db-url
    (jdbc/with-query-results res
      ["select * from bookmark where user_id = ? order by created desc" user-id]
      (vec res))))

(defn insert-bookmark- [m]
  (jdbc/with-connection db/db-url
    (jdbc/update-or-insert-values :bookmark
                                  ["url=?" (:url m)]
                                  m)))

(defn update-tags [bookmark-id tags]
  (jdbc/with-connection db/db-url
    (jdbc/transaction
     (jdbc/delete-rows :bookmark_tag ["bookmark_id=?" bookmark-id])
     (apply jdbc/insert-records :bookmark_tag
            (map #(hash-map :bookmark_id bookmark-id :tag %) tags)))))

(defn insert-bookmark [m]
  (let [tags (-> m :tags (str/split #" ")
                 (->> (remove str/blank?))
                 seq)
        m (dissoc m :tags)]
    (jdbc/with-connection db/db-url
      (jdbc/transaction
       (let [bm (insert-bookmark- m)
             bookmark-id (:bookmark_id bm)]
         (when tags (update-tags bookmark-id tags)))))))

(defn delete [bookmark-id]
  (jdbc/with-connection db/db-url
    (jdbc/delete-rows :bookmark
                      ["bookmark_id=?" bookmark-id])))

















































