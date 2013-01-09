(ns shumark.app
  (:use [ring.util.response :only [redirect response content-type]]
        [compojure [core :only [defroutes GET POST ANY]]]
        [hiccup [core :only [html]] [def :only [defhtml]]
         [page :only [html5 include-css include-js]]
         [element :only [link-to]]
         [form :only [form-to label hidden-field text-area text-field reset-button submit-button]]]
        [valip.core :only [validate]]
        [ring.util.anti-forgery]
        [ring.middleware.anti-forgery])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [clojure.string :as str]
            [shumark.model [bookmark :as model] [user :as user]]
            [shumark.openid :as openid]
            [shumark.util.http :as http]))

(defn- js []
  "
$(function() { // on document ready
  $('#add-bm-modal').on('shown', function () { $('#name').focus(); });
});


function saveBookmarkModalAjax(formName,url,msgName,modalBodyName) {
  var form_data = $('#'+formName).serialize();
  $.ajax({
    type: 'Post',
    url: url,
    dataType: 'json',
    data: form_data,
    error: function() {
      $('#'+msgName).text('Error connecting to server!!');
    },
    success: function(data) {
      if (data.errors) {
        $('#'+modalBodyName).html(data.html);
      }
      else {
        location.reload();
      }
    }
  });
}

function delBmModalForm(formId,url,msgId) {
  var form_data = $('#'+formId).serialize();
  $.ajax({
    type: 'Post',
    url: url,
    dataType: 'json',
    data: form_data,
    error: function() {
      $('#'+msgId).text('Error connecting to server!!');
    },
    success: function(data) {
      if (data.errors) {
        $('#'+msgId).text(data.errors);
      }
      else {
        var modalId = $('#'+formId+ ' input[name=modal-id]').val();
        $('#'+modalId).modal('hide');
        var editDelSpanId = $('#'+formId+ ' input[name=edit-del-span-id]').val();
        $('#'+editDelSpanId).remove();        
      }
    }
  });
}
")

(defn layout [& {nav :nav content :content}]
  (html5 {:lang :en}
         [:head
          [:meta {:charset :utf-8}]
          [:title "Shumark"]
          [:meta {:name :viewport :content "width=device-width, initial-scale=1.0"}]
          (include-css "/css/bootstrap.css" "/css/bootstrap-responsive.css")
          (include-js "/js/jquery.js")
          [:script {:type "text/javascript"} (js)]]
         [:body 
          [:div.navbar.navbar-inverse.navbar-static-top
           [:div.navbar-inner
            [:div.container-fluid
             [:a.btn.btn-navbar {:data-toggle :collapse :data-target :.nav-collapse}
              (repeat 3 [:span.icon-bar])]
             (link-to {:class :brand} "/" "Shumark")
             nav]]]
          [:div.container-fluid
           content
           [:hr]
           [:footer [:p "Copyright © Si Yu 2013"]]]          
          (include-js "/js/bootstrap.js")]))

(defn control-error
  "Take coll of errors and join them together in inline help. (from Remix)"
  [errors]
  (when-not (empty? errors) [:span.help-inline (str/join \space errors)]))

(defhtml save-bm-modal-body [modal-msg-id & [params errors]]
  [:div {:id modal-msg-id}]
  (for [[k l] [[:name "Name"] [:url "URL"] [:tags "Tags"]]]
    [(if (empty? (k errors)) :div.control-group :div.control-group.error)
     (label {:class :control-label} k l)
     [:div.controls (text-field k (k params)) (control-error (k errors))]])
  [(if (empty? (:notes errors)) :div.control-group :div.control-group.error)
   (label {:class :control-label} :notes "Notes")
   [:div.controls (text-area :notes (:notes params)) (control-error (:notes errors))]])

(defn- gen-modal-id [id] (str id "-modal"))

(defhtml save-bm-modal [id modal-label & [params]]
  (let [form-id (str id "-form")
        modal-id (gen-modal-id id)
        modal-body-id (str id "-modal-body")
        modal-label-id (str id "-modal-label")
        modal-msg-id (str id "-modal-msg")]
    (form-to {:id form-id :class :form-horizontal
               :onsubmit (str "saveBookmarkModalAjax('" form-id "','/add','" modal-msg-id "','" modal-body-id "');return false;")}
              [:post "#"]
              (anti-forgery-field)
              (hidden-field :modal-msg-id modal-msg-id)
              [:div.modal.hide.fade {:id modal-id :tabindex -1 :role :dialog :aria-labelledby modal-label-id :aria-hidden :true}
               [:div.modal-header
                [:button {:type :button :class :close :data-dismiss :modal :aria-hidden :true} "x"]
                [:h3 {:id modal-label-id} modal-label]]
               [:div.modal-body {:id modal-body-id} (save-bm-modal-body modal-msg-id params)]
               [:div.modal-footer
                (reset-button {:class :btn :data-dismiss :modal :aria-hidden :true} "Cancel")
                (submit-button {:class "btn btn-primary"} "Save Bookmark")]])))

(defn- del-bm-modal-id [bm]
  (str "del-bm-modal-" (:bookmark-id bm)))

(defn- edit-del-span-id [bm]
  "id used to remove the edit and delete span after delete."
  (str "edit-del-span-" (:bookmark-id bm)))

(defhtml del-bm-modal [bm]
  (let [bm-id (:bookmark-id bm)
        form-id (str "del-bm-modal-form-" bm-id)
        msg-id (str "msg-id-" bm-id)
        modal-id (del-bm-modal-id bm)
        onsubmit-str (str "delBmModalForm('" form-id "','/delete','" msg-id "');return false;")]
    (form-to {:id form-id :class :form-horizontal :onsubmit onsubmit-str} [:post "#"]
             (anti-forgery-field)
             (hidden-field :bookmark-id bm-id)
             (hidden-field :modal-id modal-id)
             (hidden-field :edit-del-span-id (edit-del-span-id bm))
             [:div.modal.hide.fade {:id modal-id :tabindex -1 :role :dialog :aria-labelledby :del-bm-modal-label :aria-hidden :true}
              [:div.modal-header
               [:button {:type :button :class :close :data-dismiss :modal :aria-hidden :true} "x"]
               [:h3#del-bm-modal-label "Delete"]]
              [:div.modal-body
               [:div {:id msg-id}]
               [:h5 "Are you sure want to delete this bookmark?"]
               [:h5 (str "[" (:name bm)) " - " (:url bm) "]"]]
              [:div.modal-footer
               (reset-button {:class :btn :data-dismiss :modal :aria-hidden :true} "Cancel")
               (submit-button {:class "btn btn-primary"} "Delete")]])))

(defn user [req]
  (get-in req [:session :user]))

(defn bookmark-page [req]
  (let [add-bm-modal-id-prefix "add-boomkark"
        add-bm-modal-id (gen-modal-id add-bm-modal-id-prefix)]
    (layout :nav
            [:div.nav-collapse.collapse
             [:ul.nav
              [:li (link-to {:data-toggle :modal} (str "#" add-bm-modal-id) "Add Bookmark")]
              [:li (link-to "/bookmark" (-> req user :email))]
              [:li (link-to "/logout" "Logout")]]]
            :content
            (html
             [:div.row-fluid
              [:div.span2]
              [:div.span8
               (let [bms (model/select (-> req user :user-id))
                     edit-bm-modal-prefix-fn #(str "edit-bookmark-id-" %)]
                 (html
                  [:div.well
                   [:table
                    (for [bm bms]
                      [:tr [:td {:style "padding-right:0.5em"} [:i.icon-star-empty]]
                       [:td {:nowrap :nowrap :width "100%"} (link-to (:url bm) (:name bm))
                        [:span "&nbsp;&nbsp;-&nbsp;&nbsp;"]
                        [:span (:url bm)]
                        [:span {:id (edit-del-span-id bm)} "&nbsp;&nbsp;-&nbsp;&nbsp;"
                         (link-to {:data-toggle :modal} (str "#" (gen-modal-id (edit-bm-modal-prefix-fn (:bookmark-id bm)))) [:i.icon-edit])
                         "&nbsp;&nbsp;"
                         (link-to {:data-toggle :modal} (str "#" (del-bm-modal-id bm)) [:i.icon-remove])]]])]]
                  (map del-bm-modal bms)
                  (map #(save-bm-modal (edit-bm-modal-prefix-fn (:bookmark-id %)) "Edit" %) bms)))]
              [:div.span2]]
             (save-bm-modal add-bm-modal-id-prefix "Add a Bookmark")))))

(defn home-page []
  (layout :content          
          [:div.hero-unit {:style "text-align:center;"}
           [:h1 "Shumark!"]
           [:p "Your ultimate bookmark web app."]
           [:p (link-to {:class "btn btn-primary btn-large"} "/login" "Login with Google Account")]]))

(defn- delete-bookmark [bookmark-id]
  (try
    (do
      (model/delete (Long. bookmark-id))
      (http/json-resp {}))
    (catch Exception e (.printStackTrace e) (http/json-resp {:errors (str e)}))))

(defn- add-bookmark [{params :params :as req}]
  (if-let [errors (validate params [:url (complement str/blank?) "URL can't be blank."]
                            [:tags #(< (count %) 30) "Tags can't be longer than 30 characters."]
                            [:notes #(< (count %) 100) "Notes can't be longer than 100 characters."])]
    (http/json-resp {:errors errors :html (save-bm-modal-body (:modal-msg-id params) params errors)})
    (do
      (let [_ (println "add-bookmark:")
            _ (println "user=" (user req))
            m (merge (select-keys (user req) [:user-id]) (select-keys params [:name :url :tags :notes]))
            m (update-in m [:url] http/transform-url)]
        (model/insert-bookmark m)
        (http/json-resp {})))))

(defn- login-success-handler
  "If it is a new user create an account and redirect to bookmark page,
   else redirect to bookmark page"
  [req]
  (let [user (user/insert-if-not-exist (get-in req [:session :auth-map :email]))
        _ (println "login-success-handler: user=" user)]
    (assoc (redirect "/bookmark")
      :session (assoc (:session req) :user user))))

(defn- login-failure-handler [req]
  (redirect "/"))

(defn- auth? [req]
  (get-in req [:session :auth-map]))

(defn wrap-auth [handler]
  (fn [req]
    (if (and (= (:uri req) "/bookmark") (not (auth? req)))
      (redirect "/")
      (handler req))))

(defroutes routes
  (GET "/" [:as req] (if (auth? req) (bookmark-page req) (home-page)))
  (GET "/bookmark" [:as req] (bookmark-page req))
  (GET "/login" [:as req] (openid/redirect->openid req "/openid-return"))
  (GET "/logout" [:as req] (assoc (redirect "/") :session nil))
  (GET "/openid-return" [:as req] (openid/verify req login-success-handler login-failure-handler))
  (POST "/add" [:as req] (add-bookmark req))
  (POST "/delete" [bookmark-id] (delete-bookmark bookmark-id))
  (route/resources "/")
  (route/not-found "Page not found"))


(def app (-> routes
             wrap-auth
             wrap-anti-forgery
             handler/site))
