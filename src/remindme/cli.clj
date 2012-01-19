(ns remindme.cli
  (:use [remindme.core])
  (:use [remindme.filestore])
  (:require [clojure.tools.cli :as cli]))

(defn display-requests
  "Prints to stdout a list of all requests and their identifiers - required for deletes"
  [rs]
  (doseq [r (requests rs)]
    (println r))) ; TODO: probably want some more interesting formatting

; -s -f a|d 'some clojure string'
(defn -main
  [& args]
  (let [[options requests banner] (cli/cli args
                          ["-s" "--store" "File name for the request store" :default "~/.remindmestore"]
                          ["-f" "--frequency" "minutes between reminder checks" :default 5])]
    (if (empty? requests) (println banner)
    ; TODO: is there a better way to nest destructuring?
    (let [[operation & details] requests
          request-store (file-store (:store options))]
      (cond (= operation "a") (append request-store (first details))
            (= operation "d") (delete request-store (first details))
            (= operation "l") (display-requests request-store)
            :else (println "operation must be a(dd) d(elete) or l(ist)"))))))

