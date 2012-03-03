public class SynchronizedAccount implements Account {
    private long balance = 0L;

    public synchronized long getBalance() {
        return balance;
    }

    public synchronized void clearBalance() {
        balance = 0;
    }

    public synchronized void insert(long amount) {
        long b = balance;
        try { Thread.sleep(1); } catch(Exception e) {}
        balance = b + amount;
    }

    public synchronized void withdraw(long amount) {
        long b = balance;
        try { Thread.sleep(1); } catch(Exception e) {}
        balance = b - amount;
    }
}