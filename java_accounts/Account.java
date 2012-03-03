public interface Account {
    public long getBalance() throws InterruptedException;
    public void insert(long amount) throws InterruptedException;
    public void withdraw(long amount) throws InterruptedException;
    public void clearBalance();
}