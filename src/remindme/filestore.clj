(ns remindme.filestore
  (:require [clojure.tools.logging :as log])
  (:use [remindme.core]))

; TODO: agent needs to add the :last-execution member to the map

(defn load-store
  "Loads the content from disk"
  [f]
  (read-string (slurp f)))

(defn save-store
  "Saves the repository data structure to disk"
  [rs f]
  (spit f (prn-str rs)))

; For now pattern will be, read full contents, change data structure, reserialize to disk replacing the file
(defn init-store
  [f]
  (if (.exists (java.io.File. f)) (throw (IllegalStateException. "File already exists"))
    (save-store [] f)))

(defn- next-id
  [rs]
  (inc (reduce #(max %1 (:id %2)) 0 rs)))

(defn file-store
  "Returns an implementation of RequestStore using the specified file path for the underlying
   storage mechanism"
  [f]
  (log/debug "Creating file store at location" f)
  (reify RequestStore
    (append [_ r] 
      (let [rs (load-store f)]
        (save-store (conj rs {:id (next-id rs) :request r}) f)))
    (requests [_]
      (load-store f))
    (request-map [_ t]
      ; TODO: need to convert last-execution to a string when saving and back to a LocalDateTime when loading
      (save-store (map t (load-store f)) f))
    (delete [_ id] 
      (save-store 
        (reduce
          #(conj %1 %2) 
          []
          (filter #(not= (:id %) (Integer/parseInt id)) (load-store f))) f))))
