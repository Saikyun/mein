(ns saikyun.mein.inspect
  (:require [saikyun.mein.component :refer-macros [defcomp]]
            [saikyun.mein.dom :as dom]
            [saikyun.mein.listening :as ls]
            
            [saikyun.mein.extra-core :as ec]
            [saikyun.mein.inspect.input :as input])
  (:require-macros [hiccups.core :as hiccups :refer [html]]))

(defonce document-listeners #js [])

(defcomp intro-comp
  [:div
   {:class "c1"
    :click #(set! (.. (.getElementById js/document "introspection-window") -style -display)
                  "none")
    :style {:background-color :white
            :padding "10px"
            :border "1px solid green"
            :display "none"}
    :id "introspection-window"}
   "data"])

(defonce introspection-div nil)

(defn init!
  []
  (set! introspection-div (dom/get-or-create "introspection"))
  
  (doseq [[event f] (input/listeners)]
    (.addEventListener js/document event f)
    (.push document-listeners f))
  
  (set! (.-innerHTML introspection-div) (html [:div intro-comp #_ spect]))  
  
  (ec/traverse-hiccup #(do (ls/add-event %) %) intro-comp))

(defn cleanup!
  []
  (doseq [l document-listeners]
    (println "removing listeners")
    (.removeEventListener js/document "keydown" l))
  (set! (.-length document-listeners) 0))
