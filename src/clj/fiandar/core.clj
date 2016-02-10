(ns fiandar.core
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [files not-found resources]]
            [ring.middleware.defaults :refer [wrap-defaults
                                              site-defaults]]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]
            [fiandar.texto :refer [texto]]
            [clojure.string :as str]))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
              connected-uids]}
      (sente/make-channel-socket! sente-web-server-adapter {})]
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids)) ; Watchable, read-only atom

(defroutes handler
  (GET "/" req (#'texto req))
  (GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post req))
  (files "/" {:root "target"})
  (resources "/" {:root "target"})
  (not-found "Page Not Found"))

(defn handle-sente-msg
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (when (and ?reply-fn ?data)
    (?reply-fn (apply str (repeat ?data "a")))))

(sente/start-server-chsk-router!
 ch-chsk handle-sente-msg)

(def app
  (wrap-defaults handler site-defaults))
