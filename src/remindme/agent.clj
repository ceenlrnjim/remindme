(ns remindme.agent
  (:use [remindme.core])
  (:require [clojure.tools.logging :as log])
  (:import [org.joda.time.format DateTimeFormat])
  (:import [org.joda.time LocalDateTime LocalTime LocalDate]))

(defn- do-checks
  "Run through all scheduled checks and see if any are eligible for execution"
  [request-store]
  (request-map 
    request-store
    (fn [r]
      (binding [*last-execution* (:last-execution r)]
        ; TODO: currently need to namespace qualify the "jeeves" macro and other core functions in strings from the file
        (log/debug "Executing: " (:request r))
        (def new-execution-time (load-string (:request r)))
        (if new-execution-time (assoc r :last-execution new-execution-time) r)))))

(defn agent-loop
  "Main execution loop for the agent.  Frequency is the number of minutes between checks.
  RequestStore is an implementation of the RequestStore protocol."
  [frequency request-store]
  (while true
    (log/debug "Checking requests...")
    (do-checks request-store)
    (log/debug "Sleeping for  minutes")
    (Thread/sleep (* frequency 1000 60))))
