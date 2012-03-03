import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockAccount implements Account {
    private long balance = 0L;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();

    public long getBalance() {
        readLock.lock();
        try { return balance; }
        finally { readLock.unlock(); }
    }

    public void clearBalance() {
        writeLock.lock();
        try { balance = 0; }
        finally { writeLock.unlock(); }
    }

    public void insert(long amount) {
        writeLock.lock();
        try {
            long b = balance;
            try { Thread.sleep(1); } catch(Exception e) {}
            balance = b + amount;    
        } finally { writeLock.unlock(); }
    }

    public void withdraw(long amount) {
        writeLock.lock();
        try {
            long b = balance;
            try { Thread.sleep(1); } catch(Exception e) {}
            balance = b - amount;
        } finally { writeLock.unlock(); }
    }
}