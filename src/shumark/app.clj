(ns shumark.app
  (:use [ring.util.response :only [redirect response]]
        [compojure [core :only [defroutes GET POST ANY]]]
        [hiccup [core :only [html]] [def :only [defhtml]]
         [page :only [html5 include-css include-js]]
         [element :only [link-to]]
         [form :only [form-to label hidden-field text-field reset-button submit-button]]]
        [valip.core :only [validate]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [shumark.model.bookmark :as model]))

(defn- js []
  "
function addBmModalForm(formName,url,msgName,modalBodyName) {
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
    }
  });
}
")

(defn control-error
  "Take coll of errors and join them together in inline help. (from Remix)"
  [errors]
  (when-not (empty? errors) [:span.help-inline (str/join \space errors)]))

(defhtml add-bm-modal-body-form [& [params errors]]
  (html
   [:div#addBmModalMsg]
   (for [[k l] [[:name "Name"] [:url "URL"]]]
     [(if (empty? (k errors)) :div.control-group :div.control-group.error)
      (label {:class :control-label} k l)
      [:div.controls (text-field k (k params)) (control-error (k errors))]])))

(defhtml add-bm-modal []
  (html
   (form-to {:id :addBmModalForm :class :form-horizontal :onsubmit "addBmModalForm('addBmModalForm','/add','addBmModalMsg','addBmModalBody');return false;"} [:post "#"]
            [:div.modal.hide.fade {:id :add-bm-modal :tabindex -1 :role :dialog :aria-labelledby :add-bm-modal-label :aria-hidden :true}
            [:div.modal-header
             [:button {:type :button :class :close :data-dismiss :modal :aria-hidden :true} "x"]
             [:h3#add-bm-modal-label "Add a Bookmark"]]
            [:div#addBmModalBody.modal-body (add-bm-modal-body-form)]
            [:div.modal-footer
             (reset-button {:class :btn :data-dismiss :modal :aria-hidden :true} "Cancel")
             (submit-button {:class "btn btn-primary"} "Add Bookmark")]])))

(defn- del-bm-modal-id [bm]
  (str "del-bm-modal-" (:bookmark_id bm)))

(defn- edit-del-span-id [bm]
  "id used to remove the edit and delete span after delete."
  (str "edit-del-span-" (:bookmark_id bm)))

(defhtml del-bm-modal [bm]
  (let [bm-id (:bookmark_id bm)
        form-id (str "delBmModalForm" bm-id)
        msg-id (str "msgId" bm-id)
        onsubmit-str (str "delBmModalForm('" form-id "'),'/delete','" msg-id "');return false;")]
    (form-to {:id form-id :class :form-horizontal :onsubmit onsubmit-str} [:post "#"]
             (hidden-field :bm-edit-del-span-id (edit-del-span-id bm))
             [:div.modal.hide.fade {:id (del-bm-modal-id bm) :tabindex -1 :role :dialog :aria-labelledby :del-bm-modal-label :aria-hidden :true}
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

(defhtml del-bm-modals [bms]
  (map del-bm-modal bms))

(defn- home-page []
  (html5 {:lang :en}
         [:head
          [:meta {:charset :utf-8}]
          [:title "Shumark"]
          [:meta {:name :viewport :content "width=device-width, initial-scale=1.0"}]
          (include-css "/css/bootstrap.css" "/css/bootstrap-responsive.css")
          [:script {:type "text/javascript"} (js)]]
         [:body 
          [:div.navbar.navbar-inverse.navbar-static-top
           [:div.navbar-inner
            [:div.container-fluid
             [:a.btn.btn-navbar {:data-toggle :collapse :data-target :.nav-collapse}
              (repeat 3 [:span.icon-bar])]
             (link-to {:class :brand} "/" "Shumark")
             [:div.nav-collapse.collapse
              [:ul.nav
               [:li (link-to {:data-toggle :modal} "#add-bm-modal" "Add Bookmark")]]]]]]
          [:div.container-fluid
           [:div.row-fluid
            [:div.span2]
            [:div.span8
             (let [bms (model/select)]
               (html
                [:div.well
                 [:table
                  (for [bm bms]
                    [:tr [:td {:style "padding-right:0.5em"} [:i.icon-star-empty]]
                     [:td {:nowrap :nowrap :width "100%"} (link-to (:url bm) (:name bm))
                      [:span "&nbsp;&nbsp;-&nbsp;&nbsp;"]
                      [:span {:color :orange} (:url bm)]
                      [:span {:id (edit-del-span-id bm)} "&nbsp;&nbsp;-&nbsp;&nbsp;" [:i.icon-edit] "&nbsp;&nbsp;"
                       (link-to {:data-toggle :modal} (str "#" (del-bm-modal-id bm)) [:i.icon-remove])]]])]]
                (del-bm-modals bms)))]
            [:div.span2]]
           (add-bm-modal)
           [:hr]
           [:footer [:p "© Si Yu 2012"]]]          
          (include-js "/js/jquery.js" "/js/bootstrap.js")]))

(defroutes routes
  (GET "/" [] (home-page))
  (POST "/add" [name url :as {params :params}] (if-let [errors (validate params
                                                                         [:url (complement str/blank?) "URL can't be blank."])]
                                                 {:status 200
                                                  :headers {"Content-Type" "application/json"}
                                                  :body (json/write-str {:errors errors :html (add-bm-modal-body-form params errors)})}
                                                (do
                                                 (model/insert {:user_id 1 :name name :url url})
                                                 {:status 200
                                                  :headers {"Content-Type" "application/json"}
                                                  :body (json/write-str {:errors nil})})))
        
  (route/resources "/")
  (route/not-found "Page not found"))


(def app (-> routes
             handler/site))
