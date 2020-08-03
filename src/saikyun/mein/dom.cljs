(ns saikyun.mein.dom)

(defn get-or-create
  ([id] (get-or-create "div" id))
  ([tag id]
   (or (.getElementById js/document id)
       (let [app (.createElement js/document tag)]
         (set! (.-id app) id)
         (.. js/document -body (appendChild app))
         app))))
