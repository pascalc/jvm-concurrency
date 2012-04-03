public interface Account {
    public float getBalance() throws InterruptedException;
    public boolean deposit(float amount) throws InterruptedException;
    public boolean withdraw(float amount) throws InterruptedException;
    public void clearBalance();
}