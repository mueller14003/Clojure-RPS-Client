(ns rps-client.core
  (:require [clojure.java.io :as io])
  (:require [clojure.repl :as repl])
  (:import (java.net Socket)))

(defn exit []
  (println "\nThanks for playing!")
  (System/exit 0))

(repl/set-break-handler! (fn [_] (exit)))

(def message-map {119 "WIN"
                  108 "LOSE"
                  100 "DRAW"
                  113 "QUIT"})

(defn receive-message [socket]
  (.read (io/reader socket)))

(defn send-rps [socket rps]
  (let [writer (io/writer socket)]
    (.write writer rps)
    (.flush writer)))

(defn get-input []
  (print ">> ")
  (flush)
  (let [input (read-line)]
    (if (= input "^C")
      "q"
      input)))

(defn -main
  [& args]
  (if (not (= (count args) 2))
    (do (println "ERROR: INVALID NUMBER OF INPUTS")
        (println "Please run again with the hostname as and port number as a command-line arguments.")
        (println "Example:\n> lein run localhost 6789")
        (System/exit 0))
    
    (with-open [client-socket (Socket. (first args) (Integer/parseInt (second args)))]

      (let [start (receive-message client-socket)]
        (if (= start 49)
          (println "Player 2 has connected!\n")
          (exit)))

      (println "'r' : Rock | 'p' : Paper | 's' : Scissors | 'q' : Quit")

      (loop [user-input (get-input)]

        (send-rps client-socket user-input)
        (println (format "Sent '%s' to the server" user-input))

        (let [received (receive-message client-socket)]
          (println (get message-map received))
          (if (= received 113)
            (exit)
            :default))

        (recur (get-input))))))
