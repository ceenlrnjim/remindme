(ns remindme.test.core
  (:import [org.joda.time LocalDateTime LocalTime LocalDate])
  (:import [org.joda.time.format DateTimeFormat])
  (:use [remindme.core])
  (:use [clojure.test]))

; TODO: test for today-at
(def today-at (ns-resolve 'remindme.core 'today-at))

(deftest test-today-at
  (let [now (LocalDateTime.)
        target (today-at "2:25pm")]
    (is (= (.toLocalDate now) (.toLocalDate target)))
    (is (= (.getHourOfDay target) 14))
    (is (= (.getMinuteOfHour target) 25))))

(defn- now-plus
  "Returns joda LocalDateTime for the specified number of minutes added to the current time"
  [mins]
  (.plusMinutes (LocalDateTime.) mins))

(defn- time-spec
  "Returns the current joda time as 'hh:mi:aa' format used in at commands"
  [ldt]
  (.print (DateTimeFormat/forPattern "hh:mmaa") ldt))

(deftest test-at-already-executed
  (binding [*last-execution* (today-at "5:00am")]
    (is (not (at "4:55am"))))
  (binding [*last-execution* (today-at "4:00am")]
    (is (at "4:55am"))))

(deftest test-at-not-yet
  (binding [*last-execution* (today-at "12:01am")]
    (let [ten-min-ahead-string (time-spec (now-plus 10))]
    ; need to get a few minutes back so that the at time is
      (is (not (at ten-min-ahead-string))))))

(deftest test-at-ready
  (binding [*last-execution* (today-at "12:01am")]
    (let [ten-min-back-string (time-spec (now-plus -10))]
      (is (at ten-min-back-string)))))

(deftest test-at-ready-with-additional
  (binding [*last-execution* (today-at "12:01am")]
    (let [ten-min-back-string (time-spec (now-plus -10))]
      (is (not (at ten-min-back-string false)))
      (is (not (at ten-min-back-string (< 10 5))))
      (is (at ten-min-back-string (< 5 10))))))


(def date-pattern (ns-resolve 'remindme.core 'date-pattern))
(deftest test-date-pattern
  (is (= (date-pattern "12/24") "mm/dd"))
  (is (= (date-pattern "12/24/2012") "mm/dd/yyyy")))

(deftest test-on-already-executed
  (binding [*last-execution* (LocalDateTime.)]
    (let [today (LocalDate.)
          today-spec (str (.getMonthOfYear today) "/" (.getDayOfMonth today) "/" (.getYear today))]
      (is (not (on today-spec))))))
      
(deftest test-on-not-yet
  (binding [*last-execution* nil] ; not yet executed
    (let [target (.plusDays (LocalDate.) 1) ; target is for tomorrow
          target-spec (str (.getMonthOfYear target) "/" (.getDayOfMonth target) "/" (.getYear target))]
      (is (not (on target-spec))))))
      
(deftest test-on-ready
  (binding [*last-execution* nil] ; not yet executed
    (let [target (LocalDate.) ; target is for today
          target-spec (str (.getMonthOfYear target) "/" (.getDayOfMonth target) "/" (.getYear target))]
      (is (on target-spec))
      (is (not (on target-spec false)))
      (is (not (on target-spec (< 10 5))))
      (is (on target-spec (< 1 5))))))

(defn- day-spec
  [jd]
  (get {1 :mon 2 :tue 3 :wed 4 :thu 5 :fri 6 :sat 7 :sun} (.getDayOfWeek jd)))

(deftest test-every-day-already-executed
  (binding [*last-execution* (LocalDateTime.)]
    (let [day-spec (day-spec (LocalDate.))]
      (println day-spec " : " *last-execution*)
      (is (not (every day-spec)))))
  (binding [*last-execution* (.minusDays (LocalDateTime.) 7)]
      (is (every (day-spec (LocalDate.))))))

(deftest test-every-day
  (binding [*last-execution* nil]
    (is (not (every (day-spec (.plusDays (LocalDate.) 1))))) ; day doesn't match
    (is (every (day-spec (LocalDate.)))) ; day matches
    (is (every (day-spec (LocalDate.)) (> 2 1))) ; day matches with valid additional condition
    (is (every (day-spec (.plusDays (LocalDate.) 7)))) ; next week
    (is (not (every (day-spec (LocalDate.)) (> 1 2)))))) ; day matches with invalid additional condition


