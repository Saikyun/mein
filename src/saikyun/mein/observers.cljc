(ns saikyun.mein.observers
  (:require [clojure.string :as str]
            
            [saikyun.mein.component :as c]
            [saikyun.mein.props :as p]
            
            [hiccups.runtime :as hrt]
            
            [clojure.pprint :refer [pp pprint]]

            #?(:cljs [miracle.save :refer-macros [save]])))

(defn kebab->event
  [s]
  (->> (name s)
       (#(str/split % "-"))
       (str/join "")))

(def watchers (atom {}))
(def to-remove #js [])

(defn notify-watchers
  [_ ref old new]
  (doseq [{:keys [f id] :as data} (get @watchers ref)]
    (let [node (.getElementById js/document id)]
      (if-not node
        (.push to-remove id)
        (f node id old new))
      
      (when-let [r (and (seq to-remove)
                        (into #{} to-remove))]
        (swap! watchers update ref (fn [ls] (into [] (remove #(r (:id %)) ls))))
        (set! (.-length to-remove) nil)))))

#?(:cljs (defn add-watcher
           [c]
           (let [{:keys [mein/watch id]} (p/props c)
                 watch-and-init
                 (fn [[ref f]]
                   (let [node (.getElementById js/document id)
                         data {:id id, :component c, :f f}]
                     (swap! watchers update ref #(vec (conj % data)))
                     (add-watch ref :watchers notify-watchers)
                     #_(cond (= true init)
                             (f @ref node id)
                             
                             (fn? init)
                             (init @ref node id))))]
             (cond (coll? (first watch)) (doall (map watch-and-init watch))
                   (some? watch)         (watch-and-init watch)))))

(defn add-event
  [c]
  #?(:cljs 
     (let [{:keys [mein/on id]} (p/props c)]
       (letfn [(event-listen
                 [e cb]
                 (if (= e :load)
                   (.addEventListener
                    js/window (kebab->event e)
                    (fn [_]
                      (cb #js {:target (.getElementById js/document id)
                               :id id})))
                   (.addEventListener 
                    (.getElementById js/document id)
                    (kebab->event e)
                    cb)))]
         (doseq [[e cb] on]
           (if (coll? cb)
             (doseq [c cb]
               (event-listen e c))
             (event-listen e cb)))))))
