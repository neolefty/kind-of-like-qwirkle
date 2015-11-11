package qwirkle.test;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Play with CyclicBarrier. */
public class TryCyclicBarrier {
    public static void main(String[] args) throws Exception {
        final CyclicBarrier x = new CyclicBarrier(2);
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(2000);
                    x.await();
                    System.out.println("Thread");
                    status("thread after", x);
                } catch (Exception e) { e.printStackTrace(); }
            }
        }.start();
        status("main before", x);
        new Thread() { @Override public void run() {
            status("outside before", x);
            try { sleep(100); } catch (InterruptedException ignored) { }
            status("outside during", x);
            try { sleep(2000); } catch (InterruptedException ignored) { }
            status("outside after", x);
        } }.start();
        try {
            x.await(0, TimeUnit.DAYS);
        } catch(TimeoutException ignored) {}
        System.out.println("Main");
        status("main after", x);
    }

    private static void status(String s, CyclicBarrier x) {
        System.out.println(s + ": " + x.getParties() + " parties, "
                + x.getNumberWaiting() + " waiting; borken? " + x.isBroken());
    }
}
