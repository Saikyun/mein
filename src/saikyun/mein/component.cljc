(ns saikyun.mein.component
  (:require [clojure.string :as str]
            [saikyun.mein.extra-core :as ec]
            [saikyun.mein.inspect.introspection :as i]
            [saikyun.mein.props :as p]
            [alc.x-as-tests.immediate :refer [run-tests!]]
            
            #?(:cljs [miracle.save :refer-macros [save]]))
  (:require-macros [saikyun.mein.component]))

(def events [:press :submit :click :change :keydown :keyup :input :load])

(defn extract-events
  [[_ props :as comp]]
  (if (not props)
    comp
    (reduce
     (fn [comp e]
       (if-let [cb (get props e)]
         (do
           (println "events ye" events e)
           (vary-meta (update comp 1 dissoc e) assoc-in [:on e] cb))
         comp))
     comp
     events)))

(defn extract-load
  [[_ props :as comp]]
  (if (not props)
    comp
    (if-let [cb (:load props)]
      (vary-meta (update comp 1 dissoc :load) assoc :load cb)
      comp)))

(def fill-out-meta p/add-spice-id)

(defn add-id
  [c]
  (if (:id c)
    c
    (p/vary-props c #(assoc % :id (:mein/id %)))))

(defn map->css
  [m]
  (when m
    (if (map? m)
      (str/join
       "\n"
       (for [[k v] m]
         (str (name k) ": " (if (number? v) v (name v)) ";")))
      (name m))))

(defn add-style
  [c]
  (p/vary-props c update :style map->css))

(comment
  (add-style [:div {:style {:border "1px solid green"}}])
  ;;=> [:div {:style "border: 1px solid green;"}]
  )

(def materalize (comp add-id
                      add-style))

(defmacro css
  [m]
  `(vary-meta ~m assoc
              :form ~(meta &form)
              :css true))

(defn add-css
  [c]
  (p/vary-props c (fn [{:keys [style] :as props}]
                    (if-not style
                      props
                      (assoc-in props [:mein/spice :style] style)))))

(defn trigger-load
  [c]
  #?(:cljs
     (let [props (p/props c)
           init (:mein/init props)
           node (.getElementById js/document (:mein/id props))]
       (save :trigg-load)
       (cond (coll? init) (doall (map #(% node) init))
             (some? init) (init node)))))

(defn hydrate
  [c]
  (ec/traverse-hiccup
   (fn [n]
     (-> n
         fill-out-meta
         add-css))
   c))

(run-tests!)
