public class SynchronizedAccount implements Account {
    private float balance = 0;

    public synchronized float getBalance() throws InterruptedException{
        Thread.sleep(1);
        return balance;
    }

    public synchronized void clearBalance() {
        balance = 0;
    }

    public synchronized boolean deposit(float amount) throws InterruptedException {
        float b = balance;
        Thread.sleep(1);
        balance = b + amount;
        return true;
    }

    public synchronized boolean withdraw(float amount) throws InterruptedException {
        if (balance - amount >= 0) {
            float b = balance;
            Thread.sleep(1);
            balance = b - amount;
            return true;
        } else {
            return false;
        }
    }
}