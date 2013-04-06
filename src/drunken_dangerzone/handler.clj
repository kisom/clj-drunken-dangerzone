(ns drunken-dangerzone.handler
  (:use compojure.core
        [hiccup.page :only (html5)]
        [hiccup.middleware :only (wrap-base-url)])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.params]
            [cheshire.core]))

(def ^:dynamic *keystore* (ref {}))

(defn parse-key [key]
  (if (and (string? key)
           (= \: (first key)))
    (keyword (clojure.string/join (rest key)))
    key))

(defn index []
  (html5 [:head
          [:meta {:charset "UTF-8"}]
          [:title "drunken-dangerzone"]]
         [:body
          [:h1 "drunken-dangerzone!"]
          [:p "This is a simple in-memory key-value store powered by Clojure."]
          [:p "Supported endpoints:"
           [:ul
            [:li "GET /key - retrieve a list of all keys stored in the server."]
            [:li "PUT /key - json-encode a list of keys and their values, and "
                 "they will be stored as long as they aren't already present."]
            [:li "POST /key - similar to PUT, but will overwrite any existing values."]
            [:li "GET /key/:id - retrieve the value associated with the id."]
            [:li "GET /keystore.json - retrieve the full keystore as json."]]]]))

(defn getkeys []
  (cheshire.core/generate-string @*keystore*))

(defn getkey [key]
  (cheshire.core/generate-string {key (get @*keystore* key)}))

(defn postkey [body]
  (let [request-body (slurp body)]
    (let [key-values (cheshire.core/parse-string request-body parse-key)]
      (dosync (commute *keystore* conj key-values))
      (cheshire.core/generate-string {"success" true "set" (map key key-values)}))))

(defn putkey [body]
  (let [request-body (slurp body)
        key-values (cheshire.core/parse-string request-body parse-key)]
    (dosync
     (commute *keystore* conj
                         (filter #(nil? (@*keystore* (first %)))
                                 key-values)))
    (cheshire.core/generate-string {"success" true
                                    "set" (map key
                                               (filter #(nil? (@*keystore* (first %)))
                                                       key-values))
                                    "not-updated" (map key
                                                       (filter #(@*keystore* (first %))
                                                               key-values))})))

(defn dump-keystore []
  (cheshire.core/generate-string @*keystore*))

(defroutes app-routes
  (GET "/" [] (index))
  (GET "/key" [] (getkeys))
  (GET "/key/:id" {{id :id} :params} (getkey id))
  (POST "/key" {body :body} (postkey body))
  (PUT "/key" {body :body} (putkey body))
  (GET "/keystore.json" [] (dump-keystore))
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
      (wrap-base-url)))
