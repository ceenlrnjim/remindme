(ns remindme.core)

; key abstractions
; - requests: something the remindme has been asked to do
; - requestStore: interface to a persistent store for requests
; - AuditLog: interface for a persistent store for audit information on what requests were fulfilled

(defprotocol RequestStore
  "Function specification for a repository that stores requests "
  (append [_ r] "Add a request to the store")
  (requests [_] "Returns a sequence of requests from the store")
  (delete [_ id] "removes the specified request from the store"))

; Functions/macros/data structures usable in defining reminders

; at - accepts a time and performs the action
; on - accepts a date (can be combined with at)
; every - accepts a frequency ( :date :mon :tues :wed :thurs :fri :sat :sun
; when - some arbitrary expression that results in a boolean
;
; Actions
; alert - takes a message (macro) and displays a dialog
; fork - takes a system call - to be executed via Runtime.exec
;
; (every :wed (at "12:35pm") (alert "it is 12:35"))
; (jane (every :wed (at "12:35")) (alert "it is 12:35")) 

(defmacro jeeves
  [condition & actions]
  `(if ~condition (do ~@actions) nil))

(def ^:dynamic *last-execution* -1)

(defmacro with-last-execution
  [millis & body]
  `(binding [*last-execution* millis]
     (do ~@body)))

(defn- today-at
  "Returns a millisecond (date) for the specified time on the current day"
  [time-spec]
  (let [daystring "yyyyMMdd"
        timestring "hh:mmaa"
        df-day (java.text.SimpleDateFormat. daystring)
        df-time (java.text.SimpleDateFormat. (str daystring timestring))]
    (.getTime 
      (.parse 
        df-time (str (.format df-day (java.util.Date.)) time-spec)))))

(defn at
  "time-spec: (1-12):(0-59)am|pm"
  ([time-spec] (at time-spec true))
  ([time-spec additional-condition]
   (let [target (today-at time-spec)]
     (cond (> *last-execution* target) false ; already executed this rule
           (> (System/currentTimeMillis) target) additional-condition
           :else false))))





