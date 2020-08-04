(ns saikyun.mein.props
  (:require [saikyun.mein.extra-core :refer [traverse-hiccup]]
            [saikyun.mein.id :as id]
            
            [alc.x-as-tests.immediate :refer [run-tests!]]))

(defn props
  [c]
  (when (and (coll? c)
             (map? (second c)))
    (second c)))

(comment
  (props [:div {:id 10} "hello"])
  ;;=> {:id 10}
  
  (:id (props [:div {:id 10}]))
  ;;=> 10
  
  (props [:div "hej"])
  ;;=> nil
  )

(defn vary-props
  [c f & args]
  (if (and (coll? c)
           (or (map? (second c))
               (and (= 2 (count c))
                    (nil? (second c)))))
    (apply update c 1 f args)
    (-> (into [(first c) (apply f nil args)] (rest c))
        (with-meta (meta c)))))

(comment
  (vary-props [:div] identity)
  ;;=> [:div nil]
  
  (vary-props [:div {:id 10}] update :id inc)
  ;;=> [:div {:id 11}]
  
  (vary-props [:div {:id 10}] update :id + 1 2 3 4)
  ;;=> [:div {:id 20}]
  )

(defn add-spice-id
  [c]
  (vary-props c (fn [{:keys [id :mein/spice] :as props}]
                  (if-let [id (or id (:id spice))]
                    (assoc-in props [:mein/spice :id] id)
                    (assoc-in props [:mein/spice :id] (id/id!))))))

(defn purge-single
  [c]
  (let [c (vary-props c dissoc :mein/spice)]
    (if (empty? (props c))
      (into [(first c)] (drop 2 c))
      c)))

(defn purge
  "Removes all :mein/spice from a hiccup component."
  [c]
  (traverse-hiccup purge-single c))

(comment
  ;; purges :mein/spice
  (purge-single [:div {:mein/spice {:id 20}}])
  ;;=> [:div]
  
  (purge [:div {:mein/spice {:id 10}}
          [:div {:mein/spice {:id 20}}]])
  ;;=> [:div [:div]]
  
  (let [c (vary-props [:div] dissoc :mein/spice)]
    (props c)
    #_    (if (empty? (props c))
            c
            c))
  
  (purge-single [:div])
  ;;=> [:div]
  )

(defn spice
  "Adds spice (e.g. interactivity) to a component."
  ([c] c)
  ([c k v]
   (-> (vary-props c update :mein/spice assoc k v)
       add-spice-id))
  ([c k v & args]
   (as-> c $
     (spice $ k v)
     (apply spice $ args))))

(defn listen
  [c l] (spice c :listen l))

(comment
  (-> (spice [:button "Click"]
             :on {:click #(println "Clicked")})
      props
      (get-in [:mein/spice :on :click])
      boolean)
  ;;=> true
  
  (-> (spice [:button "Click"]
             :on {:click #(println "Clicked")}
             :id "clicky")
      props
      (#(and (get-in % [:mein/spice :on :click])
             (get-in % [:mein/spice :id])))
      boolean)
  ;;=> true
  )

(run-tests!)
