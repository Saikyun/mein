(ns saikyun.mein.core
  (:require [saikyun.mein.component :as cs]
            [saikyun.mein.listening :as ls]
            [saikyun.mein.props :as p]
            [saikyun.mein.id :as id]
            [saikyun.mein.inspect :as inspect]
            [saikyun.mein.extra-core :as ec])
  #?(:cljs (:require-macros [hiccups.core :as hiccups :refer [html]])))

(defn cleanup!
  []
  #_(reset! id/id 0)
  (reset! ls/listeners {})
  (inspect/cleanup!))

(defn render
  [target view]
  #?(:cljs (do (doseq [{:keys [node problem path]} (ec/validate-hiccup view)]
                 (js/console.error "Node: " (str node))
                 (js/console.error "Has problem: " problem)
                 (js/console.error "In path: " (str (filter some? (map (comp :form meta) path))))
                 (js/console.error "Full tree: " (str path)))
               (let [view (if-not (:component (meta view))
                            (cs/component view)
                            view)
                     view (ec/traverse-hiccup cs/materalize view)]
                 
                 (set! (.-innerHTML target) (html (p/purge view)))
                 
                 (ec/traverse-hiccup #(do (ls/add-event %) %) view)  
                 (ec/traverse-hiccup #(do (cs/trigger-load %) %) view)  
                 (ec/traverse-hiccup #(do (ls/add-listener %) %) view))
               
               (inspect/init!))))
