(ns fiandar.core
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require
   [cljs.core.async :as async :refer (<! >! put! chan)]
   [taoensso.sente :as sente :refer (cb-success?)]))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk" {:type :auto})]
  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state))   ; Watchable, read-only atom

(defn add-message [msg]
  (let [c (.. js/document (createElement "DIV"))]
    (aset c "innerHTML" (str "<p>" msg "</p>"))
    (.. js/document (getElementById "container") (appendChild c))))

(defn main []
  (add-message "I am dynamically created!"))

(def counter (atom 0))

(defn reload []
  (let [count (swap! counter inc)]
    (chsk-send!
     [:minha-app/letras count]
     8000
     (comp add-message str))))
