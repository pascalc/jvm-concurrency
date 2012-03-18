(ns accounts)
(import '(java.util.concurrent Executors))
(import '(java.util.concurrent TimeUnit))

; Account manipulation functions
(defn get-balance [balance]
    (dosync
        (. Thread sleep 1)
        @balance))

(defn insert [balance amount]
    (dosync
        (. Thread sleep 1)
        (alter balance + amount)))

(defn withdraw [balance amount]
    (dosync
        (. Thread sleep 1)
        (alter balance - amount)))

; Create a thread pool
(defn make-pool [threads]
    (Executors/newFixedThreadPool threads))

; Execute f x times on y (virtual) threads
(defn dothreads! [f pool & { thread-count :threads
                  exec-count :times
                  :or {thread-count 1 exec-count 1}}]
    (dotimes [t thread-count]
        (.submit pool #(dotimes [_ exec-count] (f)))))

; Execute on the pool and return the time taken
(defn do-pool! [threads f]
    (let [pool (make-pool threads)]
        (time
            [(f pool)
            (.shutdown pool)
            (.awaitTermination pool 5 TimeUnit/SECONDS)])))

; Read frenzy
(defn read-frenzy! []
    (let [balance (ref 0)]
        (do-pool! 10 
            (fn [pool]
                (dothreads! #(insert balance 1) pool :threads 1 :times 10)
                (dothreads! #(get-balance balance) pool :threads 9 :times 10)))
        (println "Balance: " @balance)))

; Write frenzy
(defn write-frenzy! []
    (let [balance (ref 0)]
        (do-pool! 10 
            (fn [pool]
                (dothreads! #(insert balance 1) pool :threads 9 :times 10)
                (dothreads! #(get-balance balance) pool :threads 1 :times 10)))
        (println "Balance: " @balance)))