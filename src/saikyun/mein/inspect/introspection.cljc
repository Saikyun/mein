(ns saikyun.mein.inspect.introspection
  (:require [clojure.string :as str]
            [saikyun.mein.extra-core :as ec]
            [saikyun.mein.collections :refer [fmap fconj]]
            [saikyun.mein.config :as config]
            [saikyun.mein.props :as p]
            [clojure.pprint :refer [pprint]]
            
            #?(:cljs [saikyun.mein.inspect.input :as input]))
  #?(:cljs (:require-macros [hiccups.core :as hiccups :refer [html]])))

#?(:cljs (defn introspect-key-down?
           []
           (aget input/keydown config/introspect-key)))

(defn inspect
  [c e]
  #?(:cljs (when (introspect-key-down?)
             (.stopPropagation e)
             (let [iw (.getElementById js/document "introspection-window")]
               #_(set! (.-innerHTML iw)
                       (with-out-str (binding [*print-meta* true]
                                       (prn c))))
               
               (set! (.-innerHTML iw) "<a onclick=\"document.getElementById('introspection-window').style.display = 'none';\" href=\"#\">=========== close =========</a>
<iframe style=\"width: 100%; height: 100%;\" src=\"http://localhost:9630/inspect\"></iframe>")
               
               (println "tapping")
               (tap> c)
               
               (set! (.. iw -style -position) "absolute")
               (set! (.. iw -style -display) "block")
               
               (set! (.. iw -style -left) "0px")
               (set! (.. iw -style -top) "0px")
               
               (set! (.. iw -style -width) "100%")
               (set! (.. iw -style -height) "100%")
               
               #_      (set! (.. iw -style -left) (str (.-pageX e) "px"))
               #_      (set! (.. iw -style -top) (str (.-pageY e) "px"))))))

(defn add-introspection
  [cmpt]
  (p/vary-props
   cmpt
   update-in [:mein/spice :on :click]
   fconj #(inspect cmpt %)))
