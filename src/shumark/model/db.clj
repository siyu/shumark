(ns shumark.model.db
    (use [environ.core]))

(def db-url (env :database-url))