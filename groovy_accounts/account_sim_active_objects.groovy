import java.util.concurrent.TimeUnit
import java.util.concurrent.ScheduledThreadPoolExecutor

import groovyx.gpars.activeobject.ActiveObject
import groovyx.gpars.activeobject.ActiveMethod

class World {
    def inhabitants = 
    [
        'members' : [],
        'brokers' : []
    ]

    def members() { inhabitants['members'] }
    def brokers() { inhabitants['brokers'] }

    void addMember(member) { 
        member.world = this
        members() << member 
    }
    void addBroker(broker) { brokers() << broker }

    def randomMember(me) {
        def other = null
        def members = members()
        while (true) {
            int index = Math.random()*members.size()
            other = members[index]
            if (other != me) break
        }
        return other
    }

    int brokerIndex = 0
    def getBroker() {
        def b = brokers()[brokerIndex]
        brokerIndex = (brokerIndex += 1) % brokers().size()
        return b
    }

    void start() { brokers().each { it.start() } }
    void stop() { brokers().each { it.stop() } }
}

class NamedActor {
    String name

    void say(message) { 
        def host = Thread.currentThread().getName()
        println "[$host] $name: $message" 
    }
    
    String toString() { "$name" }
}

@ActiveObject
class Broker extends NamedActor {
    def running = false

    @ActiveMethod
    void transfer(from, to, float amount) {
        if (!running) { return }

        say "Sending $amount from $from to $to"
        def success = from.withdraw(amount)
        if (success) { to.deposit(amount) }
    }

    @ActiveMethod
    void start() { say "Starting"; running = true }

    @ActiveMethod
    void stop() { say "Stopping"; running = false }
}

@ActiveObject
abstract class TickingActor extends NamedActor {
    static final TIMER_THREADS = 2
    static final ScheduledThreadPoolExecutor TIMER = 
        new ScheduledThreadPoolExecutor(TIMER_THREADS)

    static TIMER_INTERVAL = 100
    static TIMER_INTERVAL_UNIT = TimeUnit.MILLISECONDS

    TickingActor() {
        TIMER.scheduleAtFixedRate(
            { this.tick() },
            0, TIMER_INTERVAL, TIMER_INTERVAL_UNIT
        )
    }

    abstract void tick();
}

@ActiveObject
class Person extends TickingActor {
    World world
    float balance

    @ActiveMethod(blocking=true)
    boolean deposit(float amount) {
        balance += amount
        say "Deposited $amount, balance is now $balance"
        return true
    }

    @ActiveMethod(blocking=true)
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

    @Override
    @ActiveMethod
    void tick() {
        int amount = Math.random()*100
        def target = world.randomMember(this)
        def broker = world.getBroker()
        broker?.transfer(this, target, amount)
    }
}

World world = new World()

1000.times { i -> world.addMember new Person(name: "Person $i", balance: 100) }
200.times { i -> world.addBroker new Broker(name: "Broker $i") }

world.start()
Thread.sleep(5000)
world.stop()
Thread.sleep(5000)

println "------------"
println "Total: " + world.members().inject(0) { total, person -> total += person.balance }
println world.members().collect { it.balance }