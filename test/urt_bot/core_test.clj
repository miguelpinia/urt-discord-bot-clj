(ns urt-bot.core-test
  (:require [clojure.test :refer :all]
            [urt-bot.core :refer :all]))

(def server-port 27960)
(def server-name "127.0.0.1")
(def pass "abcdef")
(def rcon-pass "abcdef")
(def received (atom nil))

(def s (udp-socket 10000))

(send-receive-rcon!
 "smite didpul"
 rcon-pass
 :socket s
 :server-name server-name
 :server-port server-port)


(def server-info
  (let [s (udp-socket 10000)
        server-name-rfa "risenfromashes.us"
        status          (send-receive-cmd!
                         "getstatus"
                         :socket s
                         :server-name server-name-rfa
                         :server-port server-port)
        map-info        (process-status status)
        players-info    (process-players status)]
    (close-socket! s)
    {:map map-info
     :players players-info}))

#_(sort-by :name compare (:players server-info))

#_(close-socket! s)

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))
