(ns shumark.model.migrate
  (:require [clojure.java.jdbc :as jdbc]
            [shumark.model.db :as db]))

(defn migrate []
  (jdbc/with-connection db/db-url
    (jdbc/do-commands "
drop table if exists bookmark_user;" "
drop table if exists bookmark;" "
create table if not exists bookmark_user (
  user_id serial primary key,
  email text not null unique,
  created timestamp not null default now());" "
create table if not exists bookmark (
  bookmark_id serial primary key,
  user_id integer,
  name text,
  url text not null,
  tags text,
  notes text,
  created timestamp not null default now(),
  modified timestamp not null default now())")))


(defn -main []
  (print "Migrating database...") (flush)
  (migrate)
  (println " done."))

(comment
  ;; CREATE DATABASE SHUMARK;
  (-main))