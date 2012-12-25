(ns shumark.model.db
    (use [environ.core]))

(def db-url (env :HEROKU_POSTGRESQL_AMBER_URL))