(ns noir-demo.views.common
  (:use [noir.core :only [defpartial]]
        [hiccup.page-helpers :only [include-css include-js html5]]
        [hiccup.form-helpers :only [form-to text-field]]))

(def navbar
  [:div.navbar
   [:div.navbar-inner
    [:div.container
     [:a.brand {:href "/todos"} "innoQ ToDo"]
     [:div.nav-collapse
      [:ul.nav
       [:li [:a {:href "/todos"} "Home"]]]
      (form-to {:class "navbar-search pull-right"}
               [:get "/todos"]
               (text-field {:class "search-query span2"
                            :placeholder "Search"}
                           "search"))]]]])

(defn layout [& content]
  (html5
   [:head
    [:title "noir-demo"]
    (include-css "/css/bootstrap.css")]
   [:body
    navbar
    [:div.container
     content]]))
