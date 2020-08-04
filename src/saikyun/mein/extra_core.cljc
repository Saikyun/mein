(ns saikyun.mein.extra-core)

(defmacro update!
  [attr & [f & args]]
  `(set! ~attr (~f ~attr ~@args)))

(defn is-hiccup?
  [node]
  (and (vector? node)
       (keyword? (first node))))

(defn traverse-hiccup
  [f node]
  (if (not (is-hiccup? node))
    node
    (let [[tag props-or-child & children :as new-node] (f node)
          [tag props children] (if (map? props-or-child)
                                 [tag props-or-child children]
                                 [tag nil (if props-or-child
                                            (concat [props-or-child] children)
                                            children)])
          children (map #(traverse-hiccup f %) children)
          new-node' (if props
                      (into [tag props] children)
                      (into [tag] children))]
      (with-meta new-node' (meta new-node)))))

(defn validate-hiccup
  ([node] (validate-hiccup node [] []))
  ([node path problems]
   (if (not (is-hiccup? node))
     (if (coll? node)
       (conj problems {:problem "Collection inside hiccup form"
                       :node node
                       :path (conj path node)})
       problems)
     (let [[tag props-or-child & children :as new-node] node
           [tag props children] (if (map? props-or-child)
                                  [tag props-or-child children]
                                  [tag {} (concat [props-or-child] children)])
           new-problems (map #(validate-hiccup % (conj path node) problems) children)]
       (apply concat problems new-problems)))))

