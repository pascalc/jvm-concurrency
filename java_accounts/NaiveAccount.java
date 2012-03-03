public class NaiveAccount implements Account {
    private long balance = 0L;

    public long getBalance() {
        return balance;
    }

    public void clearBalance() {
        balance = 0;
    }

    public void insert(long amount) throws InterruptedException {
        long b = balance;
        Thread.sleep(1);
        balance = b + amount;
    }

    public void withdraw(long amount) throws InterruptedException {
        long b = balance;
        Thread.sleep(1);
        balance = b - amount;
    }
}