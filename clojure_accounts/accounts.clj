(ns accounts)
(import '(java.util.concurrent Executors))
(import '(java.util.concurrent TimeUnit))

; dosync and count retries
(defmacro spy-dosync [& body]
  `(let [retries# (atom -1)
         result# (dosync
                   (swap! retries# inc)
                   ~@body)]
     (println "retry count:" @retries#)
     result#))

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

(defn transfer [balance1 balance2 amount]
    (dosync
        (withdraw balance1 amount)
        (. Thread sleep 1)
        (insert balance2 amount)))

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

; Mutual transfer - can cause deadlock in Java
(defn mutual-transfer! []
    (let [balance1 (ref 10) 
          balance2 (ref 10)]
        (do-pool! 2
            (fn [pool]
                (dothreads! #(transfer balance1 balance2 5) pool :threads 1 :times 1)
                (dothreads! #(transfer balance2 balance1 5) pool :threads 1 :times 1)))
        (println "Balance1: " @balance1 ", Balance2: " @balance2)))

(defn sleepy-inc [a]
    (Thread/sleep 1)
    (inc a))

(defn inc-atom! []
    (let [x (atom 0)]
        (println "x: " @x)
        (do-pool! 10
            (fn [pool]
                (dothreads! #(swap! x sleepy-inc) pool :threads 10 :times 1)))
        (println "x: " @x)))

(defn mk-balance [b]
    (let [balance (ref b)]
        (set-validator! balance #(>= % 0))
        balance))

(defn donate [donor receiver]
    (dosync 
        (alter receiver inc)
        (alter donor dec)))

(defn donation [donor-balances]
    (let [donors (map mk-balance donor-balances)
          receiver (mk-balance 0)]
        (doseq [d donors]  
            (try 
                (donate d receiver)
                (catch IllegalStateException e)))
        (println "donors:" donors)
        (println "receiver:" receiver)))

(defn time-agent [times sleep]
    (let [x (agent 0)]
        (dotimes [_ times]
            (send-off x
                (fn [x]
                    (Thread/sleep sleep)
                    (inc x))))
        (time (await x))
        @x))
