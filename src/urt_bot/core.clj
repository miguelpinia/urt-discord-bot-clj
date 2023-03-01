(ns urt-bot.core
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint-indent]]
            [udp-wrapper.core :refer [create-udp-server close-udp-server
                                      packet get-bytes-utf8 make-address
                                      send-message receive-message-data
                                      receive-message empty-packet]]

            [clojure.string :as str])
  (:gen-class))

(def udp-socket create-udp-server)
(def close-udp-socket close-udp-server)
(def prefix (byte-array [0xff 0xff 0xff 0xff]))


(defn rcon-command [command rcon-pass]
  (byte-array (concat prefix
                      (get-bytes-utf8
                       (format "rcon %s %s\n"
                               rcon-pass
                               command)))))

(defn command [command]
  (byte-array (concat prefix (get-bytes-utf8 command))))

(defn send!
  "Send a message into the urban terror server. The message can be a
  rcon command or a basic command. The socket is an udp socket with
  the server-name and server-port where the message will be sent."
  [message & {:keys [socket server-name server-port]}]
  (let [payload (packet message
                        (make-address server-name)
                        server-port)]
    (send-message socket payload)))

(defn receive!
  [socket]
  (let [response (empty-packet 8192)]
    (receive-message-data socket response)))

(defn send-receive-rcon!
  [command rcon-pass & {:keys [socket server-name server-port] :as info-socket}]
  (let [message  (rcon-command command rcon-pass)
        response (empty-packet 8192)]
    (send! message info-socket)
    (receive! socket)))

(defn send-receive-cmd!
  [cmd & {:keys [socket server-name server-port] :as info-socket}]
  (let [message (command cmd)
        response (empty-packet 8192)]
    (send! message info-socket)
    (receive! socket)))

(defn process-status
  [status]
  (-> status
      (s/split #"\n")
      (second)
      (s/trim)
      (s/split #"\\")
      (rest)
      (as-> r (partition 2 2 r))
      (as-> prt (map (fn [[k v]] [(keyword (s/trim k)) v]) prt))
      (as-> tpls (into {} tpls))))

(defn process-players
  [status]
  (-> status
      (s/split #"\n")
      (as-> data (drop 2 data))
      (as-> players (map (fn [info-player] (s/split info-player #" ")) players))
      (as-> new-plyrs (map (fn [info] {:score (Integer/parseInt (first info))
                                    :ping (Integer/parseInt (second info))
                                    :name (s/replace (nth info 2) #"\"" "")})
                         new-plyrs))))

(defn close-socket!
  [socket]
  (close-udp-socket socket))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
