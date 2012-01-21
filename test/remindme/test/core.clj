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

(defn- joda-to-millis [jt] (.getTime (.toDate jt)))

(deftest test-at-already-executed
  (binding [*last-execution* (today-at "5:00am")]
    (is (not (at "4:55am"))))
  (binding [*last-execution* (today-at "4:00am")]
    (is (at "4:55am"))))

;(deftest test-at-not-yet
;(deftest test-at-ready
;(deftest test-at-ready-with-additional

