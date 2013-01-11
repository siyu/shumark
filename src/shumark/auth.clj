(ns shumark.auth
  (:use [ring.util.response :only [redirect]])
  (:require [shumark.model.user :as user]))


(defn user [req]
  (get-in req [:session :user]))

(defn login-success-handler
  "If it is a new user create an account and redirect to bookmark page,
   else redirect to bookmark page"
  [req]
  (let [user (user/insert-if-not-exist (get-in req [:session :auth-map :email]))
        _ (println "login-success-handler: user=" user)]
    (assoc (redirect "/bookmark")
      :session (assoc (:session req) :user user))))

(defn login-failure-handler
  "Handler for failed login attempt."
  [req]
  (redirect "/"))

(defn auth?
  "Check if a request is authenticated."
  [req]
  (get-in req [:session :auth-map]))

(defn wrap-auth [handler]
  "Authentication wrapper."
  (fn [req]
    (if (and (= (:uri req) "/bookmark") (not (auth? req)))
      (redirect "/")
      (handler req))))