package org.Kroj.Core.Tools.ObjectManagement.ObjectManager;


import org.Kroj.Core.Statics.Initializer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.Kroj.Core.Tools.Logger.Logger.logger;


public class ObjectPool<T> {

    private final BlockingQueue<T> bq;
    private final ObjectFactory<T> of;
    private final int ps;

    public ObjectPool(ObjectFactory<T> object, int poolSize) {
        this.of = object;
        this.ps = poolSize;
        this.bq = new LinkedBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            bq.add(of.create());
        }
    }

    private final AtomicInteger i = new AtomicInteger(0);

    public T get() {
        try {
            T obj;
            if ((obj = bq.poll(5, TimeUnit.MILLISECONDS)) == null) return of.create();
            return obj;
        } catch (InterruptedException e) {
            logger.error().append("Pool borrow interrupted, Trying Again").nextLine();
            if (i.getAndIncrement() > 10) {
                logger.error().append("Failed Even with 10 tries, Please Check The Code").nextLine();
                System.exit(Initializer.OBJECTPOOL_FAILED);
                return null;
            }
            return get();
        }    }

    public void back(T t) {
        try {
            if (bq.size() < ps && !bq.offer(t, 5, TimeUnit.MILLISECONDS)) {
                logger.error().append("Pool Full, Fail to offer the object").nextLine();
            }
        } catch (InterruptedException e) {
            logger.error().append("Pool get interrupted, Trying Again").nextLine();
        }
    }

}

    /*
    public static void main(String[] args) throws InterruptedException {
        for (int mew = 0; mew < 100; mew++) {
            ObjectPool<StringBuilder> sbPool = new ObjectPool<>(StringBuilder::new, 8);

            ExecutorService pool = Executors.newFixedThreadPool(8);

            AtomicLong maxNS = new AtomicLong(0);

            for (int t = 0; t < 8; t++) {
                pool.execute(() -> {
                    long now = System.nanoTime();
                    for (int i = 0; i < 100000; i++) {
                        StringBuilder builder = sbPool.get();
                        builder.setLength(0);
                        builder.append(i);
                        sbPool.back(builder);
                    }
                    long time = System.nanoTime() - now;
                    if (maxNS.get() < time) maxNS.set(time);
                });
            }

            pool.shutdown();
            pool.awaitTermination(1, TimeUnit.MINUTES);

            System.out.println("MT: " + maxNS.get() / 1_000_000.0 + " ms");

            long last = System.nanoTime();
            for (int t = 0; t < 8; t++) {
                for (int i = 0; i < 100000; i++) {
                    StringBuilder builder = sbPool.get();
                    builder.setLength(0);
                    builder.append(i);
                    sbPool.back(builder);
                }
            }
            System.out.println("ST: " + (System.nanoTime() - last) / 1_000_000.0 + " ms");
        }
    }
}
*/