(ns shumark.model.db
  (:use [environ.core])
  (:require [clojure.java.jdbc :as jdbc]))

(def db-url (env :database-url))

(def naming-strategy {:keyword #(-> % clojure.string/lower-case (clojure.string/replace \_ \-))
                      :entity #(clojure.string/replace % \- \_)})

(defmacro with-db
  "Evaluate the body in the context of jdbc with-connecting using db-url."
  [& body]
  `(if (jdbc/find-connection)
     (do ~@body)
     (jdbc/with-naming-strategy naming-strategy 
       (jdbc/with-connection db-url
         ~@body))))