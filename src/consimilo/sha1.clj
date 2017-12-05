(ns consimilo.sha1)

(defn- get-hash
  [type data]
  (.digest (java.security.MessageDigest/getInstance type) (.getBytes data)))

(defn- sha1-hash
  [data]
  (get-hash "sha1" data))

(defn get-hash-int
  [data]
  (->> (sha1-hash data)
    biginteger))

