(ns saikyun.mein.listening
  (:require [clojure.string :as str]
            
            [saikyun.mein.collections :refer [fconj fmap!]]
            [saikyun.mein.component :as c]
            [saikyun.mein.props :as p]
            
            [hiccups.runtime :as hrt]
            
            [clojure.pprint :refer [pp pprint]]))

(defn kebab->event
  [s]
  (->> (name s)
       (#(str/split % "-"))
       (str/join "")))

(def listeners (atom {}))
(def to-remove #js [])

(defn notify-listeners
  [_ ref _ new]
  (doseq [{:keys [f node id] :as data} (get @listeners ref)]
    (if-not (.-parentNode node)
      (.push to-remove id)
      (f new data)))
  
  (when-let [r (and (seq to-remove)
                    (into #{} to-remove))]
    (swap! listeners update ref (fn [ls] (into [] (remove #(r (:id %)) ls))))
    (set! (.-length to-remove) nil)))

#?(:cljs (defn add-listener
           [c]
           (let [{:keys [listen id]} (:mein/spice (p/props c))
                 listen-and-init
                 (fn [{:keys [atom f init]}]
                   (let [node (.getElementById js/document id)
                         data {:id id, :component c, :f f, :node node}]
                     (swap! listeners update atom #(vec (conj % data)))
                     (add-watch atom :listeners notify-listeners)
                     (cond (= true init)
                           (f @atom data)
                           
                           (fn? init)
                           (init @atom data))))]
             
             (if (map? listen)
               (listen-and-init listen)
               (fmap! listen-and-init listen)))))

(defn add-event
  [c]
  #?(:cljs (let [{:keys [on id]} (:mein/spice (p/props c))]
             (doseq [[e cb] on]
               (fmap! #(.addEventListener (.getElementById js/document id) (kebab->event e) %) cb)))))
