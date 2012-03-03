public class NaiveAccount implements Account {
    private long balance = 0L;

    public long getBalance() {
        return balance;
    }

    public void clearBalance() {
        balance = 0;
    }

    public void insert(long amount) {
        long b = balance;
        try { Thread.sleep(1); } catch(Exception e) {}
        balance = b + amount;
    }

    public void withdraw(long amount) {
        long b = balance;
        try { Thread.sleep(1); } catch(Exception e) {}
        balance = b - amount;
    }
}