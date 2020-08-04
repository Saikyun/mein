(ns saikyun.mein.inspect.input)

(def keydown #js {})

(defn listeners
  []
  [["keydown" #(aset keydown (.-key %) true)]
   ["keyup"   #(aset keydown (.-key %) false)]])
