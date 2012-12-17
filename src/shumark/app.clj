(ns shumark.app
  (:use [ring.util.response :only [redirect response]]
        [compojure [core :only [defroutes GET POST ANY]]]
        [hiccup [core :only [html]] [def :only [defhtml]]
         [page :only [html5 include-css include-js]]
         [element :only [link-to]]
         [form :only [form-to label text-field]]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [clojure.data.json :as json]
            [shumark.model.bookmark :as model]))

(defn- js []
  "
function addBmModalForm(formName,url,msgName,modalBodyName) {
  alert('js');
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
")

(defhtml add-bm-modal-body-form []
  (html
   [:div#addBmModalMsg]
   (for [[k l] [[:name "Name"] [:url "URL"]]]
     [:div.control-group
      (label {:class :control-label} k l)
      [:div.controls (text-field k)]])
   ))

(defhtml add-bm-modal []
  (html
   (form-to {:id :addBmModalForm :class :form-horizontal :onsubmit "addBmModalForm('addBmModalForm','/add','addBmModalMsg','addModalBody');return false;"} [:post "#"]
            [:div.modal.hide.fade {:id :add-bm-modal :tabindex -1 :role :dialog :aria-labelledby :add-bm-modal-label :aria-hidden :true}
            [:div.modal-header
             [:button {:type :button :class :close :data-dismiss :modal :aria-hidden :true} "x"]
             [:h3#add-bm-modal-label "Add a Bookmark"]]
            [:div#addBmModalBody.modal-body (add-bm-modal-body-form)]
            [:div.modal-footer
             [:button.btn {:data-dismiss :modal :aria-hidden :true} "Cancel"]
             [:button.btn.btn-primary "Add Bookmark"]]])))

(defn- home-page []
  (html5 {:lang :en}
         [:head
          [:meta {:charset :utf-8}]
          [:title "Shumarkt"]
          [:meta {:name :viewport :content "width=device-width, initial-scale=1.0"}]
          (include-css "/css/bootstrap.css" "/css/bootstrap-responsive.css")
          [:script {:type "text/javascript"} (js)]]
         [:body 
          [:div.navbar.navbar-inverse.navbar-static-top
           [:div.navbar-inner
            [:div.container
             [:a.btn.btn-navbar {:data-toggle :collapse :data-target :.nav-collapse}
              (repeat 3 [:span.icon-bar])]
             (link-to {:class :brand} "/" "Shumark")
             [:div.nav-collapse.collapse
              [:ul.nav
               [:li (link-to {:data-toggle :modal} "#add-bm-modal" "Add Bookmark")]]]]]]
          [:div.container
           [:div.row
            [:div.span12
             [:div.well
              [:ul
               (for [bm (model/select)] [:li (:url bm)])]]]]
           (add-bm-modal)
           [:hr]
           [:footer [:p "© Si Yu 2012"]]]          
          (include-js "/js/jquery.js" "/js/bootstrap.js")]))

(defroutes routes
  (GET "/" [] (home-page))
  (GET "/add" [name url :as {params :params}] (do (model/insert {:user_id 1 :name name :url url}) (redirect "/")))
  (POST "/add" [name url :as {params :params}] (do (prn "params=" params)
                                                 (model/insert {:user_id 1 :name name :url url})
                                                   {:status 200
                                                    :headers {"Content-Type" "application/json"}
                                                    :body (json/write-str {:errors nil})}))
  (route/resources "/")
  (route/not-found "Page not found"))


(def app (-> routes
             handler/site))
