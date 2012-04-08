(ns account-sim)

(import '(java.util.concurrent Executors))
(import '(java.util.concurrent TimeUnit))

; Person state machine

(defrecord Person [name balance]) ; Shows refs can wrap custom data types
(defn make-person [name balance]
    (let [person (ref (Person. name balance))]
        (set-validator! person 
            (fn [new-state] (>= (:balance new-state) 0)))
        (add-watch person :print-balance
            (fn [k p old-state new-state] 
                (let [n (:name new-state)
                      b1 (:balance old-state)
                      b2 (:balance new-state)]
                    (println n ": balance" b1 "->" b2))))
        person))

; Transitions

(defn deposit [person amount]
    (dosync
        (let [balance (:balance @person)]
        (alter person assoc :balance (+ balance amount)))))

(defn withdraw [person amount]
    (dosync
        (let [balance (:balance @person)]
        (alter person assoc :balance (- balance amount)))))

; Transactions

(defn transfer [sender receiver amount]
    (dosync
        (deposit receiver amount)
        (withdraw sender amount)))

; World

(defn schedule [f timeline period]
    (.scheduleAtFixedRate timeline f 0 period TimeUnit/MILLISECONDS))

(defn make-people [num start-balance]
    (map 
        #(make-person (str "Person " %) start-balance) 
        (range num))) 

(defn rand-other [coll member]
    (let [others (filter #(not (= member %)) coll)]
        (rand-nth others)))

; Simulation

(def TICK-INTERVAL 100) 
(defn start [me people timeline]
    (schedule
        (fn [] 
            (let [target (rand-other people me)
                  amount (rand-int 100)]
                (try 
                    (transfer me target amount)
                    (println "Transferred" amount 
                        "from" me "to" target)
                    (catch IllegalStateException e))))
        timeline TICK-INTERVAL))

(def NUM-PEOPLE 100)
(def START-BALANCE 100)
(defn simulate []
    (let [people (make-people NUM-PEOPLE START-BALANCE)
          timeline (Executors/newScheduledThreadPool 2)]
        (doseq [p people] (start p people timeline))
        
        (Thread/sleep 1000)
        
        (.shutdown timeline)
        (.awaitTermination timeline 5 TimeUnit/SECONDS)
        
        (let [balances (map #(:balance @%) people)]
            (println "Balances:" balances)
            (println "Total:" (reduce + balances)))))
