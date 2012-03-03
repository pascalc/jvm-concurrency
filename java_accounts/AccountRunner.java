import java.util.Arrays;

public class AccountRunner {
    
    public static long[] testCorrectness(Account account) 
    throws InterruptedException {
        final Account a = account;
        long[] balances = new long[10];

        for (int trial = 0; trial < 10; trial++) {
            Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        for (int i =0; i < 10; i++)
                            a.insert(1);
                    } catch (InterruptedException e) {}
                }
            });

            Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        for (int i =0; i < 10; i++)
                            a.withdraw(1);
                    } catch (InterruptedException e) {}
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

    public static long readFrenzy(Account account) 
    throws InterruptedException {
        final Account a = account;
        Thread[] readers = new Thread[10];
        Thread writer;

        for(int r = 0; r < 10; r++) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    for (int i = 0; i < 9; i++) {
                        try {
                            a.getBalance();
                            Thread.sleep(1);
                        } catch (InterruptedException e) {}
                    }
                }
            });
            readers[r] = t;
        }

        writer = new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        a.insert(1);
                        Thread.sleep(1);
                    } catch (InterruptedException e) {}
                }
            }
        });

        long startTime = System.currentTimeMillis();
        try { 
            for(Thread t : readers) t.start();
            writer.start();

            for (Thread t: readers) t.join();
            writer.join();
        } catch(Exception e) {}
        long timeTaken = System.currentTimeMillis() - startTime;

        return timeTaken;
    }

    public static void main(String[] args) throws InterruptedException {
        long[] naiveResults = testCorrectness(new NaiveAccount());
        System.out.println("Naive Account Results: " + Arrays.toString(naiveResults));

        long[] synchronizedResults = testCorrectness(new SynchronizedAccount());
        System.out.println("Synchronized Account Results: " + Arrays.toString(synchronizedResults));

        long[] readWriteLockResults = testCorrectness(new ReadWriteLockAccount());
        System.out.println("Read-Write-Lock Account Results: " + Arrays.toString(readWriteLockResults));

        System.out.println();

        long synchronizedReadTime = readFrenzy(new SynchronizedAccount());
        System.out.println("Synchronized Read Frenzy: " + synchronizedReadTime + " ms");

        long readWriteLockReadTime = readFrenzy(new ReadWriteLockAccount());
        System.out.println("Read-Write-Lock Read Frenzy: " + readWriteLockReadTime + " ms");
    }
}