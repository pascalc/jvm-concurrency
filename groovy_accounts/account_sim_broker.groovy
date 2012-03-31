import groovyx.gpars.actor.Actors
import groovyx.gpars.actor.DefaultActor

// Declare messages
final class TransferRequest { def from; def to; float amount }
final class Deposit { float amount }
final class Withdraw { float amount }
final class Start {}
final class Stop {}

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

    void start() { 
        inhabitants.each { name, list -> list*.start() } 
        inhabitants.each { name, list -> list*.send new Start() } 
    }
    void stop() { 
        brokers().each { it.send new Stop() }
        Thread.sleep 1000
        members().each { it.send new Stop() }
    }
}

class AccountActor extends DefaultActor {
    String name
    boolean running = false
    void say(message) { println "${name}: ${message}" }
    void onException(Throwable e) { println "Caught $e" }
    String toString() { "$name" }
}

class Broker extends AccountActor {
    // Actions
    void transfer(from, to, float amount) {
        if (!running) { return }

        say "Sending $amount from $from to $to"
        def success = from.sendAndWait new Withdraw(amount: amount)
        if (success) { to.sendAndWait new Deposit(amount: amount) } 
    }

    // Lifecycle
    void act() {
        loop {
            react { 
                switch(it) {
                    case Start:
                        say "Starting"
                        running = true
                        break
                    case TransferRequest:
                        transfer(it.from, it.to, it.amount)
                        break
                    case Stop:
                        say "Stopping"
                        running = false
                        break;
                }
            }
        }
    }
}

class Person extends AccountActor {
    // State
    World world
    float balance

    // Actions
    boolean deposit(float amount) {
        if (!running) { return }

        balance += amount
        say "Deposited $amount, balance is now $balance"
        return true
    }

    boolean withdraw(float amount) {
        if (!running) { return }

        if (balance - amount >= 0) {
            balance -= amount
            say "Withdrew $amount, balance is now $balance"
            return true
        } else {
            say "That's more than I have!!"
            return false
        }
    }

    void requestTransfer() {
        if (!running) { return }

        int amount = Math.random()*100
        def target = world.randomMember(this)
        def broker = world.getBroker()
        broker?.send new TransferRequest(from: this, to: target, amount: amount)
    }

    // Handle messages
    def handle(message) { 
        switch(message) {
            case Start:
                say "Starting"
                running = true
                break
            case Deposit:
                reply deposit(message.amount)
                break
            case Withdraw: 
                reply withdraw(message.amount)
                break
            case Stop:
                say "Stopping"
                running = false
                break
        }
    }

    // Lifecycle
    void act() {
        loop {
            Thread.sleep(10)
            requestTransfer()
            react { message -> handle(message) }
        }
    }
}

Actors.defaultActorPGroup.resize 12

World world = new World()

10.times { i -> world.addMember new Person(name: "Person $i", balance: 100) }
2.times { i -> world.addBroker new Broker(name: "Broker $i") }

world.start()
Thread.sleep(1000)
world.stop()

println "------------"
println "Total: " + world.members().inject(0) { total, person -> total += person.balance }