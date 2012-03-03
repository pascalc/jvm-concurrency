import java.util.Arrays;

public class AccountRunner {
    
    public static long[] testCorrectness(Account account) {
        final Account a = account;
        long[] balances = new long[10];

        for (int trial = 0; trial < 10; trial++) {
            Thread t1 = new Thread(new Runnable() {
                public void run() {
                    for (int i =0; i < 10; i++)
                        a.insert(1);
                }
            });

            Thread t2 = new Thread(new Runnable() {
                public void run() {
                    for (int i =0; i < 10; i++)
                        a.withdraw(1);
                }
            });

            try { 
                t1.start(); t2.start();
                t1.join(); t2.join(); 
            } catch(Exception e) {}

            balances[trial] = a.getBalance();
            a.clearBalance();
        }

        return balances;
    }

    public static void main(String[] args) {
        long[] naiveResults = testCorrectness(new NaiveAccount());
        System.out.println("Naive Account Results: " + Arrays.toString(naiveResults));

        long[] synchronizedResults = testCorrectness(new SynchronizedAccount());
        System.out.println("Synchronized Account Results: " + Arrays.toString(synchronizedResults));

        long[] readWriteLockResults = testCorrectness(new ReadWriteLockAccount());
        System.out.println("Read-Write-Lock Account Results: " + Arrays.toString(readWriteLockResults));
    }
}