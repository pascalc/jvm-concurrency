public class NaiveAccount implements Account {
    private float balance = 0;

    public float getBalance() {
        return balance;
    }

    public void clearBalance() {
        balance = 0;
    }

    public boolean deposit(float amount) throws InterruptedException {
        float b = balance;
        Thread.sleep(1);
        balance = b + amount;
        return true;
    }

    public boolean withdraw(float amount) throws InterruptedException {
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