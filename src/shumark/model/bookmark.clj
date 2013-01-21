(ns shumark.model.bookmark
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            [shumark.model.db :as db]
            [shumark.util.mapper :as mapper]))

(defn bookmark-mapper [res]
  (->> res
       (mapper/reduce-rows {:parent-keys [:bookmark-id :user-id :name :url :notes] :children-keys [:tag]})
       (map #(assoc % :tags (apply str (interpose " " (map :tag (:children %)))) ))))

(defn select-all [user-id limit offset]
  (db/with-db
    (jdbc/with-query-results res
      ["select b.bookmark_id, b.user_id, b.name, b.url, b.notes, bt.tag
        from bookmark b left outer join bookmark_tag bt on (b.bookmark_id = bt.bookmark_id)
        where b.user_id = ?
        order by b.created desc limit ? offset ?" user-id limit offset]
      (bookmark-mapper res))))

(defn select-all-cnt [user-id]
  (db/with-db
    (jdbc/with-query-results res
      ["select count(*) as total
        from bookmark b left outer join bookmark_tag bt on (b.bookmark_id = bt.bookmark_id)
        where b.user_id = ?" user-id]
      (-> res first :total))))

(defn select-by-tag-cnt [user-id tag]
  (db/with-db
    (jdbc/with-query-results res
      ["select count(*) as total
        from bookmark b inner join bookmark_tag bt on (b.bookmark_id = bt.bookmark_id)
        where b.user_id = ?
          and bt.tag = ?" user-id tag]
      (-> res first :total))))

(defn select-by-tag [user-id tag limit offset]
  (db/with-db
    (jdbc/with-query-results res
      ["select b.bookmark_id, b.user_id, b.name, b.url, b.notes, bt.tag
        from bookmark b inner join bookmark_tag bt on (b.bookmark_id = bt.bookmark_id)
        where b.user_id = ?
          and bt.tag = ? order by b.created desc limit ? offset ?" user-id tag limit offset]
      (bookmark-mapper res))))

(defn insert-bookmark- [m]
  (db/with-db
    (jdbc/update-or-insert-values :bookmark
                                  ["url=?" (:url m)]
                                  m)))

(defn update-tags
  "Delete all existing tags associated with the bookmark-id and insert the provided tags."
  [bookmark-id tags]
  (db/with-db
    (jdbc/transaction
     (jdbc/delete-rows :bookmark_tag ["bookmark_id=?" bookmark-id])
     (when tags
       (apply jdbc/insert-records :bookmark_tag
              (map #(hash-map :bookmark-id bookmark-id :tag %) tags))))))

(defn insert-bookmark
  "Insert the bookmark m on database if it does not exist, otherwise update it."
  [m]
  (let [tags (-> m :tags (str/split #" ")
                 (->> (remove str/blank?))
                 seq)
        m (dissoc m :tags)]
    (db/with-db
      (jdbc/transaction
       (let [bm (insert-bookmark- (dissoc m :bookmark-id))
             bookmark-id (or (:bookmark-id m) (:bookmark-id bm))]
         (update-tags (Integer. bookmark-id) tags))))))

(defn select-tags [user-id]
  (db/with-db
    (jdbc/with-query-results res
      ["select bt.tag, count(bt.tag) as count
          from bookmark b inner join bookmark_tag bt on (b.bookmark_id = bt.bookmark_id)
         where b.user_id = ?
         group by bt.tag" user-id]
      (->> res
          (map (juxt :tag :count))
          (sort-by second)
          reverse))))

(defn delete [bookmark-id]
  (db/with-db
    (jdbc/delete-rows :bookmark
                      ["bookmark_id=?" bookmark-id])))
