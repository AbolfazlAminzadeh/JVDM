package org.kroj.Core.Tools.TestUnit;

import java.util.concurrent.atomic.AtomicLong;

public class Tester {

    private static final AtomicLong currentNano = new AtomicLong(0);
    public static void speedTest(int count, Runnable r) {
        final int finalCount = Math.max(count, 2);

        try {
            new Thread(() -> {
                try {
                    while (true) {
                        int currentCount = 0;
                        double nano = currentNano.get()/1000000D;
                        System.out.printf("SpeedTest Result %d: %.8fn\r",++currentCount, nano);
                        Thread.sleep(1000);
                        System.out.print("\033[H\033[2J");
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            while (true) {
                long last = System.nanoTime();

                for (int k = 0; k < finalCount; k++) {
                    r.run();
                }

                long now = System.nanoTime();
                long nanosPerRun = (long) ((double) (now - last) / finalCount * 1000000);

                currentNano.set(nanosPerRun);
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
