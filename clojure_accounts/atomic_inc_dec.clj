(ns accounts)

(defn insert [balance amount]
    (dosync
        (println "Going to insert" amount)
        (. Thread sleep 100)
        (alter balance + amount)
        (println "Inserted" amount)))

(defn withdraw [balance amount]
    (dosync
        (println "Going to withdraw" amount)
        (. Thread sleep 100)
        (alter balance - amount)
        (println "Withdrew" amount)))

(def balance (ref 0))
(println "Balance is" @balance)

(future (insert balance 1))
(future (withdraw balance 1))

(. Thread sleep 1000)

(println "Finishing balance:" @balance)
