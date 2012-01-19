(ns remindme.core)

; key abstractions
; - requests: something the remindme has been asked to do
; - requestStore: interface to a persistent store for requests
; - AuditLog: interface for a persistent store for audit information on what requests were fulfilled

(defprotocol RequestStore
  "Function specification for a repository that stores requests "
  (append [_ r] "Add a request to the store")
  (requests [_] "Returns a sequence of requests from the store")
  (delete [_ r] "removes the specified request from the store"))
