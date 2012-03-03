public class SynchronizedAccount implements Account {
    private long balance = 0L;

    public synchronized long getBalance() throws InterruptedException{
        Thread.sleep(10);
        return balance;
    }

    public synchronized void clearBalance() {
        balance = 0;
    }

    public synchronized void insert(long amount) throws InterruptedException {
        long b = balance;
        Thread.sleep(1);
        balance = b + amount;
    }

    public synchronized void withdraw(long amount) throws InterruptedException {
        long b = balance;
        Thread.sleep(1);
        balance = b - amount;
    }
}