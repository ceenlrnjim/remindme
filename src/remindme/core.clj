(ns remindme.core
  (:import [org.joda.time.format DateTimeFormat])
  (:import [org.joda.time LocalDateTime LocalTime LocalDate]))

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

; 
; Shared plumming used by the agent to run commands
;
(defmacro jeeves
  [condition & actions]
  `(if ~condition (do ~@actions) nil))

; LocalDateTime of the last execution
(def ^:dynamic *last-execution* nil)

(defmacro with-last-execution
  "converts millis to joda localdatetime and binds for functions to use"
  [millis & body]
  `(binding [*last-execution* (if millis (LocalDateTime. millis) nil)]
     (do ~@body)))

(defn- today-at
  "Returns a joda DateTime (date) for the specified time on the current day"
  [time-spec]
  (let [d (LocalDate.)
        t (.parseLocalTime (DateTimeFormat/forPattern "hh:mmaa") time-spec)]
    (.toLocalDateTime d t)))

(defn at
  "time-spec: (1-12):(0-59)am|pm"
  ([time-spec] (at time-spec true))
  ([time-spec additional-condition]
   (let [target (today-at time-spec)
         now (LocalDateTime.)]
     (cond (.isAfter *last-execution* target) false ; already executed this rule
           (.isAfter now target) additional-condition ; if we are at the specified time (after technically), then go if the other condition is true
           :else false))))

(defn- date-pattern
  "Checks whether or not a year as been added for the date format string"
  [spec]
  (if (= (.indexOf spec "/") (.lastIndexOf spec "/")) "mm/dd" "mm/dd/yyyy"))

(defn on
  "Date spec: mm/dd(/yyyy)"
  ([date-spec] (on date-spec true))
  ([date-spec additional-condition]
   (if *last-execution* false ; if this has already been executed, skip it - doesn't really matter when
     (let [now (LocalDate.)
           ; TODO: currently only works with full date format including year
           target (.parseLocalDate (DateTimeFormat/forPattern (date-pattern date-spec)) date-spec)]
       (and (.equals now target) additional-condition)))))




