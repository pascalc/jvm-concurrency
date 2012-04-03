import java.util.Arrays;

public class AccountRunner {
    
    public static float[] testCorrectnessSingle(Account account) 
    throws InterruptedException {
        final Account a = account;
        a.deposit(10);
        float[] balances = new float[10];

        for (int trial = 0; trial < 10; trial++) {
            Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        for (int i =0; i < 10; i++)
                            a.deposit(1);
                        for (int i =0; i < 10; i++)
                            a.withdraw(1);
                    } catch (InterruptedException e) {}
                }
            });

            try { 
                t1.start(); t1.join();
            } catch(Exception e) {}

            balances[trial] = a.getBalance();
            a.clearBalance();
            a.deposit(10);
        }

        return balances;
    }

    public static float[] testCorrectnessMulti(Account account) 
    throws InterruptedException {
        final Account a = account;
        a.deposit(10);
        float[] balances = new float[10];

        for (int trial = 0; trial < 10; trial++) {
            Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        for (int i =0; i < 10; i++)
                            a.deposit(1);
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
            a.deposit(10);
        }

        return balances;
    }

    public static float readFrenzy(Account account) 
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
                        a.deposit(1);
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

    public static float writeFrenzy(Account account) 
    throws InterruptedException {
        final Account a = account;
        Thread[] writers = new Thread[10];
        Thread reader;

        for(int w = 0; w < 10; w++) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    for (int i = 0; i < 9; i++) {
                        try {
                            a.deposit(1);
                            Thread.sleep(1);
                        } catch (InterruptedException e) {}
                    }
                }
            });
            writers[w] = t;
        }

        reader = new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < 10; i++) {
                    try {
                        a.getBalance();
                        Thread.sleep(1);
                    } catch (InterruptedException e) {}
                }
            }
        });

        long startTime = System.currentTimeMillis();
        try { 
            for(Thread t : writers) t.start();
            reader.start();

            for (Thread t: writers) t.join();
            reader.join();
        } catch(Exception e) {}
        long timeTaken = System.currentTimeMillis() - startTime;

        return timeTaken;
    }

    public static void deadlock(final Account from, final Account to,
        final float amount) throws InterruptedException {
        
        Thread[] transferThreads = new Thread[2];
        transferThreads[0] = new Thread(new Runnable() {
            public void run() {
                try { transfer(from, to, amount); }
                catch(Exception e) {}
            }
        });
        transferThreads[1] = new Thread(new Runnable() {
            public void run() {
                try { transfer(to, from, amount); }
                catch(Exception e) {}
            }
        });

        for(Thread t : transferThreads) {
            t.start();
        }

        for(Thread t : transferThreads) {
            t.join();
        }
    }

    private static void transfer(Account from, Account to, float amount) 
        throws InterruptedException {
        
        synchronized(from) {
            Thread.sleep(1);
            synchronized(to) {
                from.withdraw(amount);
                to.deposit(amount);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // float[] naiveResults = testCorrectnessSingle(new NaiveAccount());
        // System.out.println("Naive Account Single Threaded Results: " + Arrays.toString(naiveResults));

        // float[] naiveResults2 = testCorrectnessMulti(new NaiveAccount());
        // System.out.println("Naive Account Multiple Threaded Results: " + Arrays.toString(naiveResults2));

        // float[] synchronizedResults = testCorrectnessMulti(new SynchronizedAccount());
        // System.out.println("Synchronized Account Results: " + Arrays.toString(synchronizedResults));

        // float[] readWriteLockResults = testCorrectnessMulti(new ReadWriteLockAccount());
        // System.out.println("Read-Write-Lock Account Results: " + Arrays.toString(readWriteLockResults));

        // System.out.println();

        // float synchronizedReadTime = readFrenzy(new SynchronizedAccount());
        // System.out.println("Synchronized Read Frenzy: " + synchronizedReadTime + " ms");

        // float readWriteLockReadTime = readFrenzy(new ReadWriteLockAccount());
        // System.out.println("Read-Write-Lock Read Frenzy: " + readWriteLockReadTime + " ms");

        // System.out.println();

        // float synchronizedWriteTime = writeFrenzy(new SynchronizedAccount());
        // System.out.println("Synchronized Write Frenzy: " + synchronizedWriteTime + " ms");

        // float readWriteLockWriteTime = writeFrenzy(new ReadWriteLockAccount());
        // System.out.println("Read-Write-Lock Write Frenzy: " + readWriteLockWriteTime + " ms");

        // System.out.println();

        Account from = new SynchronizedAccount();
        from.deposit(10);
        Account to = new SynchronizedAccount();
        to.deposit(10);
        deadlock(from, to, 10);
    }
}