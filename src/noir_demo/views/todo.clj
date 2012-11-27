(ns noir-demo.views.todo
  (:require [noir-demo.views.common :as common]
            [noir-demo.models.todo :as model]
            [noir.validation :as vali]
            [noir.options :as opts])
  (:use [noir.core :only [defpage defpartial render]]
        [noir.response :only [redirect]]
        [noir.server :only [add-middleware]]
        [hiccup.core :only [html escape-html]]
        [hiccup.form-helpers :only [form-to submit-button label text-field]]
        [clojure.pprint :only [pprint]]))

;; ## HTML generation

(defn table-row
  "transforms a todo item to a table row, e.g.:

    {:id 4711
     :title \"title of the todo item\"
     :description \"a description of the task\"
     :status \"current status of the task\"}

   is transformed into:

    [:tr
     [:td \"title of the todo item\"]
     [:td \"a description of the task\"]
     [:td \"current status of the task\"]
     [:td
      [:form
       {:method \"GET\",
        :action \"/todos/4711\",
        :style \"display:inline\",
        :id \"change\"}]
      [:form
       {:method \"POST\",
        :action \"/todos/4711\",
        :style \"display:inline\",
        :id \"delete\"}
       [:input
        {:name \"_method\", :type \"hidden\", :id \"_method\", :value \"DELETE\"}]]
      [:div.btn-group
       [:button.btn {:form \"change\"} \"Ändern\"]
       [:button.btn {:form \"delete\"} \"Löschen\"]]]]"
  [{:keys [title description status id]}]
  [:tr
   [:td (escape-html title)]
   [:td (escape-html description)]
   [:td (escape-html status)]
   [:td
    (form-to {:style "display:inline"
              :id (str "change" id)}
             [:get (str "/todos/" id)])
    (form-to {:style "display:inline"
              :id (str "delete" id)}
             [:delete (str "/todos/" id)])
    [:div.btn-group
     [:button.btn {:form (str "change" id)} "Change"]
     [:button.btn {:form (str "delete" id)} "Delete"]]]])

(defn todo-table
  "transforms a collection of todos into a html table"
  [todos]
  [:table.table
   [:thead
    [:tr
     [:th "Titel"]
     [:th "Beschreibung"]
     [:th "Status"]
     [:th ""]
     [:th ""]]]
   [:tbody
    (map table-row todos)]])

(defn control-group [errors vali-key & inputs]
  (let [error (first (get errors vali-key))
        class (if error "control-group error" "control-group")]
    [:div {:class class}
     inputs
     (when error [:span.help-inline error])]))

(defn todo-fieldset [errors {:keys [title description status]}]
  [:fieldset
   (control-group errors :noir-demo.models.todo/title
                  (label "title" "Title*")
                  (text-field "title" title))
   (control-group errors :noir-demo.models.todo/description
                  (label "description" "Description")
                  (text-field "description" description))
   (control-group errors :noir-demo.models.todo/status
                  (label "status" "Status*")
                  (text-field "status" status))])

(defn create-todo-form
  "form to create a todo item"
  [errors todo]
  (form-to {:class "well"}
           [:post "/todos"]
           (todo-fieldset errors todo)
           (submit-button {:class "btn btn-primary"} "Add")))

(defn edit-todo-form
  "form to edit a todo item"
  [errors todo]
  (form-to {:class "well"}
           [:put (str "/todos/" (:id todo))]
           (todo-fieldset errors todo)
           [:button {:class "btn btn-primary"
                     :type "submit"} "Save"]
           [:button {:class "btn"
                     :href "/todos"} "Cancel"]))

(defn todo-page
  "renders a page with a list of todo items and a form to create a new todo item"
  ([todos]
     (todo-page todos nil nil))
  ([todos errors todo]
     (common/layout
      [:div.row
       [:div.span8
        [:h2 "Current ToDos"]
        [:div.well
         (todo-table todos)]]
       [:div.span4
        [:h2 "New ToDo"]
        (create-todo-form errors todo)]])))

;; ## Routing

(defn noir-errors
  "utility function to access the errors collected through noir's validation mechanism"
  []
  @noir.validation/*errors*)

(defpage "/" []
  (redirect "/todos"))

;; collection of todo items
(defpage "/todos" {search :search}
  (if search
    (todo-page (model/find-todos search))
    (todo-page (model/all))))

;; adding a new todo item
(defpage [:post "/todos"] {:as new-todo}
  (if (model/valid? new-todo)
    (do (model/add new-todo)
        (redirect "/todos"))
    (todo-page (model/all) (noir-errors) new-todo)))

;; a form to edit a todo item
(defpage "/todos/:id" {id :id}
  (let [todo (model/by-id (Integer. id))]
    (common/layout
     (edit-todo-form nil todo))))

;; updating a todo item
(defpage [:put "/todos/:id"] {:keys [id title description status]}
  (let [edited-todo {:id (Integer. id)
                     :title title
                     :description description
                     :status status}]
    (if (model/valid? edited-todo)
      (do (model/modify edited-todo)
          (redirect "/todos"))
      (common/layout
       (edit-todo-form (noir-errors) edited-todo)))))

;; deleting a todo item
(defpage [:delete "/todos/:id"] {id :id}
  (model/remove (Integer. id))
  (redirect "/todos"))