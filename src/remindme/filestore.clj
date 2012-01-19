(ns remindme.filestore
  (:require [clojure.tools.logging :as log])
  (:use [remindme.core]))

(defn file-store
  "Returns an implementation of RequestStore using the specified file path for the underlying
   storage mechanism"
  [f]
  (log/debug "Creating file store at location" f)
  (reify RequestStore
    ; TODO: add actual implementation
    (append [_ r] (println "saving" r))
    (requests [_] [])
    (delete [_ id] (println "deleting id" id))))
