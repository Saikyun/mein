(ns saikyun.mein.inspect.introspection
  (:require [saikyun.mein.extra-core :as ec]
            [saikyun.mein.collections :refer [fmap fconj]]
            [saikyun.mein.config :as config]
            #?(:cljs [saikyun.mein.inspect.input :as input])))

#?(:cljs (defn introspect-key-down?
           []
           (aget input/keydown config/introspect-key)))

(defn inspect
  [c e]
  (println "inspecting")

  #?(:cljs (when (introspect-key-down?)
             (let [iw (.getElementById js/document "introspection-window")]
               #_(set! (.-innerHTML iw)
                       (with-out-str (binding [*print-meta* true]
                                       (prn c))))
               
               (set! (.-innerHTML iw) "<a href=\"#\">=========== close =========</a>
<iframe style=\"width: 100%; height: 100%;\" src=\"http://localhost:9630/inspect\"></iframe>")
               
               (println "tapping")
               (tap> (ec/traverse-hiccup (fn [c] (ec/update-props c assoc :meta (meta c))) c))
               
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
  (vary-meta 
   cmpt
   (fn [c]
     (-> (update-in c [:on :click] fconj #(inspect cmpt %))
         (assoc :component true)))))
