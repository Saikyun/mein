(ns saikyun.mein.inspect.input)

(def keydown #js {})

(defn listeners
  []
  [["keydown" #(do (println "down") (aset keydown (.-key %) true))]
   ["keyup"   #(aset keydown (.-key %) false)]])
