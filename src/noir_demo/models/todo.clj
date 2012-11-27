(ns noir-demo.models.todo
  (:refer-clojure :exclude [remove])
  (:require [noir.validation :as vali]
            [clojure.java.jdbc :as sql])
  (:use [korma.db :only [defdb]]
        [korma.config :only [set-delimiters]]
        [korma.core :only [defentity pk table entity-fields database has-one has-many belongs-to transform
                           select insert values order where update set-fields delete]]))

(def db-conf {:classname "org.hsqldb.jdbc.JDBCDriver"
              :subprotocol "hsqldb"
              :subname "db/test;hsqldb.write_delay=false"
              :user "sa"
              :password ""})

(defn create-todo-table []
  (sql/with-connection db-conf
    (sql/transaction
     (sql/create-table :todos
                       [:id "BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) PRIMARY KEY"]
                       [:title "VARCHAR(500)"]
                       [:description "VARCHAR(500)"]
                       [:edited "BIGINT"]
                       [:status "VARCHAR(500)"]))))

(defn drop-todo-table []
   (sql/with-connection db-conf
    (sql/transaction
     (sql/drop-table :todos))))

(try 
  (create-todo-table)
  (catch Exception e))

(defn to-lower-case [k]
  (-> k name .toLowerCase keyword))

(defn lower-case-keys [m]
  (into {} (map (fn [[k v]] [(to-lower-case k) v]) m)))

(defdb mydb db-conf)

(set-delimiters "")

(defentity todos
  (entity-fields :title :description :status :edited)
  (transform lower-case-keys))

(defn edited [todo]
  (assoc todo :edited (System/currentTimeMillis)))

(defn all []
  (select todos
          (order :edited :DESC)))

(defn match? [search {:keys [title description status]}]
  (let [s (.toLowerCase search)
        content (-> (str title description status)
                    .toLowerCase)]
    (.contains content s)))

(defn find-todos [search]
  (->> (all)
       (filter (partial match? search))))

(defn add [todo]
  (insert todos (values
                 (edited todo))))

(defn by-id [id]
  (first
   (select todos
           (where {:id id}))))

(defn modify [{:keys [id title description status]}]
  (update todos
          (set-fields {:edited (System/currentTimeMillis)
                       :title title
                       :description description
                       :status status})
          (where {:id id})))

(defn remove [id]
  (delete todos
          (where {:id id})))

(defn valid? [{:keys [title description status]}]
  (vali/rule (vali/min-length? title 5)
             [::title "Please enter a title with at least 5 characters."])
  (vali/rule (vali/has-value? status)
             [::status "Please enter a status."])
  (not (vali/errors? ::title ::status)))