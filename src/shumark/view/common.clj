(ns shumark.view.common
  (:use [hiccup [element :only [link-to]]
         [util :only [url]]])
  (:require [clojure.string :as str]))

(defn control-error
  "Take coll of errors and join them together in inline help. (from Remix)"
  [errors]
  (when-not (empty? errors) [:span.help-inline (str/join \space errors)]))

(defn make-paging
  "Given a url and display-per-page, it returns a function that takes a total-cnt,
   curr-page and more-params and returns a map with paging component, num-pages,
   start-row, and end-row."
  [paging-url display-per-page]
  (fn [total-cnt curr-page & [more-params]]
    (let [num-pages (int (/ total-cnt display-per-page))
          num-pages (if (> (mod total-cnt display-per-page) 0) (inc num-pages) num-pages)
          [start-row end-row] (if (>= curr-page 0)
                                [(* curr-page display-per-page) (dec (* (inc curr-page) display-per-page))]
                                [0 display-per-page])
          pager [:ul.pager
                 [:li (link-to (url paging-url (merge more-params {:curr-page 0})) "<<")]
                 [:li (link-to (url paging-url (merge more-params {:curr-page (max 0 (dec curr-page))})) "<")]
                 [:li (link-to (url paging-url (merge more-params {:curr-page (min (dec num-pages) (inc curr-page))})) ">")]
                 [:li (link-to (url paging-url (merge more-params {:curr-page (dec num-pages)} more-params)) ">>")]]]
      {:pager pager :start-row start-row :end-row end-row :num-pages num-pages :display-per-page display-per-page})))