import java.util.concurrent.TimeUnit
import groovyx.gpars.actor.DefaultActor

class World {
    def inhabitants = [:]

    void add(member) { 
        member.world = this
        inhabitants.get(member.class, []) << member 
    }
    void leftShift(member) { add(member) }

    def randomOther(me) {
        def other = null
        while (true) {
            def list = inhabitants[me.class]
            int index = Math.random()*list.size()
            other = list[index]
            if (other != me) break
        }
        return other
    }

    def get(type) { inhabitants[type] }
    def getAt(type) { get(type) }

    void start() {
        inhabitants.each { it.value*.start() }
    }

    void join () { inhabitants.each { it.value*.join() } }
    void join(timeout, unit) {
        inhabitants.each { it.value*.join(timeout, unit) }
    }
}

class Person extends DefaultActor {
    // Declare messages
    final class Deposit { float amount }
    final class Withdraw { float amount }

    // State
    World world
    String name
    float balance
    int lifetime = 10

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
            return false
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
        loop(lifetime) {
            transfer(world.randomOther(this), (Math.random()*50).toInteger())
            react { message -> handle(message) }
        }
    }

    void afterStop(List undeliveredMessages) {
        undeliveredMessages.each { handle(it) }
    }

    void onException(Throwable e) {}

    String toString() { "$name@$world" }
}

World world = new World()
world << new Person(name: "Pascal", balance: 100)
world << new Person(name: "Phelan", balance: 100)

world.start()
world.join(1, TimeUnit.SECONDS)  // <- Works but times out
// DEADLOCK -> world.join()

println "------------"
println "Total: " + world[Person].inject(0) { total, person -> total += person.balance }