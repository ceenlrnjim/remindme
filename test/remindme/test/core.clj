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

