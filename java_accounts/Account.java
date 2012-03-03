public interface Account {
    public long getBalance();
    public void insert(long amount);
    public void withdraw(long amount);
    public void clearBalance();
}