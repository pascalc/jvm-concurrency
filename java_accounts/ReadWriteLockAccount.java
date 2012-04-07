import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockAccount implements Account {
    private float balance = 0;

    private final ReentrantReadWriteLock rwl = 
        new ReentrantReadWriteLock();
    private final Lock readLock = rwl.readLock();
    private final Lock writeLock = rwl.writeLock();

    public float getBalance() throws InterruptedException {
        readLock.lock();
        try { 
            Thread.sleep(1);
            return balance; 
        }
        finally { readLock.unlock(); }
    }

    public void clearBalance() {
        writeLock.lock();
        try { balance = 0; }
        finally { writeLock.unlock(); }
    }

    public boolean deposit(float amount) 
    throws InterruptedException {
        writeLock.lock();
        try {
            float b = balance;
            Thread.sleep(1);
            balance = b + amount;    
            return true;
        } finally { writeLock.unlock(); }
    }

    public boolean withdraw(float amount) 
    throws InterruptedException {
        writeLock.lock();
        try {
            if (balance - amount >= 0) {
                float b = balance;
                Thread.sleep(1);
                balance = b - amount;
                return true;
            } else {
                return false;
            }
        } finally { writeLock.unlock(); }
    }
}