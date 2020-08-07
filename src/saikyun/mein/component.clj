(ns saikyun.mein.component
  (:require [saikyun.mein.extra-core :as ec]
            [saikyun.mein.inspect.introspection :as i]
            [saikyun.mein.props :as p]))

(defn c-form
  [c extra-meta]
  `(-> ~c
       (#(into [] %))
       (p/vary-props update-in [:mein/spice :meta] #(merge % (meta ~c) ~extra-meta))
       fill-out-meta
       (#(ec/traverse-hiccup add-css %))
       i/add-introspection))

(defmacro component
  [c]
  (c-form c {:form (meta &form)}))

(defmacro defcomp
  [sym doc-or-args args-or-first-form & body]
  (let [[doc args body]
        (if (string? doc-or-args)
          [doc-or-args args-or-first-form body]
          [nil doc-or-args (concat [args-or-first-form] body)])
        body (c-form `(do ~@body) (-> {:form (meta &form)}
                                      (#(if doc (assoc % :doc doc) %))))]
    (if doc
      `(defn ~sym ~doc ~args ~body)
      `(defn ~sym ~args ~body))))

