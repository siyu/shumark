(ns shumark.app
  (:use [compojure [core :only [defroutes GET ANY]]]
        [hiccup [page :only [html5 include-css include-js]]
         [element :only [link-to]]])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [shumark.model.bookmark :as model]))



(defn- home-page []
  (html5 {:lang :en}
         [:head
          [:meta {:charset :utf-8}]
          [:title "Shumarkt"]
          [:meta {:name :viewport :content "width=device-width, initial-scale=1.0"}]
          (include-css "/css/bootstrap.css" "/css/bootstrap-responsive.css")
          [:script {:type "text/javascript"} ""]]
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
           [:div.modal.hide.fade {:id :add-bm-modal :tabindex -1 :role :dialog :aria-labelledby :add-bm-modal-label :aria-hidden :true}
            [:div.modal-header
             [:button {:type :button :class :close :data-dismiss :modal :aria-hidden :true} "x"]
             [:h3#add-bm-modal-label "Add a Bookmark"]]
            [:div.modal-body
             [:p "body"]]
            [:div.modal-footer
             [:button.btn {:data-dismiss :modal :aria-hidden :true} "Cancel"]
             [:button.btn.btn-primary "Add Bookmark"]]]
           [:hr]
           [:footer [:p "© Si Yu 2012"]]]          
          (include-js "/js/jquery.js" "/js/bootstrap.js")]))

(defroutes routes
  (GET "/" [] (home-page))
  (route/resources "/")
  (route/not-found "Page not found"))


(def app (-> routes
             handler/site))
