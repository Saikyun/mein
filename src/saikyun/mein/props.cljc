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
  (vary-props c (fn [props]
                  (assoc props :mein/id (or (:id props) (:mein/id props) (id/id!))))))

(defn remove-namespaced-keys
  [m]
  (->> (remove (comp namespace key) m)
       (into {})))

(comment
  (remove-namespaced-keys {::a 10, :b 20})
  ;;=> {:b 20}
  )

(defn purge-single
  [c]
  (let [c (vary-props c remove-namespaced-keys)]
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
   (let [k (if (namespace k) 
             k
             (keyword "mein" (name k)))]
     (-> (vary-props c assoc k v)
         add-spice-id)))
  ([c k v & args]
   (as-> c $
     (spice $ k v)
     (apply spice $ args))))

(defmacro watch
  "Appends props to `c`, which results in:
  Whenever reference `ref` is modified,
  Run `f` with arguments: dom-node, id, old-value, new-value
  This signature is similar to the watcher function of: `clojure.core/add-watch`
  The difference being that the first argument `dom-node` replaces the reference argument in `add-watch`."
  [c ref f]
  `(vary-props ~c update :mein/watch #(vec (conj % ~[ref f]))))

(defmacro init
  "Appends props to `c`, which results in:
  When component `c` is first rendered,
  call `f` with the resulting dom node as argument."
  [c f]
  `(vary-props ~c update :mein/init #(vec (conj % ~f))))

(comment
  (def text (atom ""))
  
  (saikyun.mein.props/watch [:div] #'text #'println)
  ;;=> [:div {:mein/watch [[#'saikyun.mein.props/ref #'cljs.core/println]]}]
  
  (let [c (saikyun.mein.props/watch [:div] text println)
        [ref cb] (get-in (props c) [:mein/watch 0])]
    [(= text ref) (= cb println)])
  ;;=> [true true]
  
  (saikyun.mein.props/init [:div] #'println)
  ;;=> [:div {:mein/init [#'cljs.core/println]}]
  )

(run-tests!)
