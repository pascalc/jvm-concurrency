import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockAccount implements Account {
    private long balance = 0L;

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();

    public long getBalance() throws InterruptedException {
        readLock.lock();
        Thread.sleep(10);
        try { return balance; }
        finally { readLock.unlock(); }
    }

    public void clearBalance() {
        writeLock.lock();
        try { balance = 0; }
        finally { writeLock.unlock(); }
    }

    public void insert(long amount) throws InterruptedException {
        writeLock.lock();
        try {
            long b = balance;
            Thread.sleep(1);
            balance = b + amount;    
        } finally { writeLock.unlock(); }
    }

    public void withdraw(long amount) throws InterruptedException {
        writeLock.lock();
        try {
            long b = balance;
            Thread.sleep(1);
            balance = b - amount;
        } finally { writeLock.unlock(); }
    }
}