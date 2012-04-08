
import java.util.concurrent.TimeUnit
import groovyx.gpars.actor.DefaultActor

class World {
    def members = []

    void add(member) { 
        member.world = this
        members << member 
    }
    void leftShift(member) { add(member) }

    def randomOther(me) {
        def other = null
        while (true) {
            int index = Math.random()*members.size()
            other = members[index]
            if (other != me) break
        }
        return other
    }

    void start() { members*.start() }
    void join () { members*.join() }
    void join(timeout, unit) { members*.join(timeout, unit) }
}

class Person extends DefaultActor {
    // Declare messages
    final class Deposit { float amount }
    final class Withdraw { float amount }

    // State
    World world
    String name
    float balance
    int lifetime = 50

    // Actions
    boolean deposit(float amount) {
        balance += amount
        say "Deposited $amount, balance is now $balance"
        return true
    }

    boolean withdraw(float amount) {
        if (balance - amount >= 0) {
            balance -= amount
            say "Withdrew $amount, balance is now $balance"
            return true
        } else {
            say "That's more than I have!!"
            return false // throw OutOfMoneyException
        }
    }

    void transfer(Person target, float amount) {
        say "Sending $amount to $target"
        def success = withdraw(amount)
        if (success) { 
            target.sendAndWait(new Deposit(amount: amount), 10, TimeUnit.MILLISECONDS) // <- Works but has to be timed out
            // DEADLOCK -> target.sendAndWait new Deposit(amount: amount)
            // INCORRECT -> target.send new Deposit(amount: amount)
        } 
    }

    void say(message) {
        println "${name}: ${message}"
    }

    // Handle messages
    def handle(message) { 
        switch(message) {
            case Deposit:
                reply deposit(message.amount)
                break
            case Withdraw: 
                reply withdraw(message.amount)
                break
        }
    }

    // Lifecycle
    void act() {
        loop({ lifetime > 0 }) {
            transfer(world.randomOther(this), (Math.random()*50).toInteger())
            react { message -> handle(message) }
            lifetime--
        }
    }

    void afterStop(List undeliveredMessages) {
        undeliveredMessages.each { handle(it) }
    }

    void onException(Throwable e) {
        println "Caught $e"
    }

    String toString() { "$name@$world" }
}

World world = new World()
world << new Person(name: "Pascal", balance: 100)
world << new Person(name: "Phelan", balance: 100)

world.start()
world.join(1, TimeUnit.SECONDS)  // <- Works but times out
// DEADLOCK -> world.join()

println "------------"
println "Total: " + world.members.inject(0) { total, person -> total += person.balance }
world.members.each { println "$it wasted $it.lifetime turns" }