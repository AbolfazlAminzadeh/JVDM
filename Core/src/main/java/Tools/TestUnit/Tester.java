package Tools.TestUnit;

import java.util.concurrent.atomic.AtomicLong;

public class Tester {

    public static void speedTest(final int count, final Runnable... r) {
        final int finalCount = Math.max(count, 100);

        try {
            AtomicLong[] res = new AtomicLong[r.length];
            for (int i = 0; i < r.length; i++) {
                res[i] = new AtomicLong();
            }

            new Thread(() -> {
                try {
                    while (true) {
                        int ct = 0;
                        for (AtomicLong rs : res) {
                            double nanos = rs.get()/1000000D;
                            System.out.printf("SpeedTest Result %d: %.8fn\r",++ct, nanos);
                        }
                        Thread.sleep(1000);
                        System.out.print("\033[H\033[2J");
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            while (true) {
                for (int i = 0; i < r.length; i++) {
                    long last = System.nanoTime();

                    for (int k = 0; k < finalCount; k++) {
                        r[i].run();
                    }

                    long now = System.nanoTime();
                    long nanosPerRun = (long) ((double) (now - last) / finalCount * 1000000);

                    res[i].set(nanosPerRun);
                }
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
