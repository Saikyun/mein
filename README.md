

### examples

```clojure
(defn replace-content [new-val data]
  (set! (.-innerHTML (:node data)) new-val))

(defn listen-replace
  [a]
  {:atom a, :f #'replace-content})

(defcomp
  playground
  [:div
   (p/spice [:button "up"]
            :on {:click #(println (swap! counter inc))})
   (p/spice [:div "hej:"]
            :listen (listen-replace counter))
   (p/spice [:div "hej:"]
            :listen (listen-replace s))
   (p/spice [:input]
            :on {:input #(do (println "wat" @s) (reset! s (.. % -target -value)))})])
```
