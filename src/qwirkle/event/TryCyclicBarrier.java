package qwirkle.event;

import java.util.concurrent.CyclicBarrier;

/** Play with CyclicBarrier. */
public class TryCyclicBarrier {
    public static void main(String[] args) throws Exception {
        final CyclicBarrier x = new CyclicBarrier(2);
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(5000);
                    x.await();
                    System.out.println("Thread");
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.start();
        x.await();
        System.out.println("Main");
    }
}
