(ns saikyun.mein.id)

(defonce id (atom 0))

(defn id! 
  ([] (id! "cid-"))
  ([prefix] (id! prefix id))
  ([prefix id-atom] (str prefix (swap! id-atom inc))))
