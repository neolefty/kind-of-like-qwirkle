package qwirkle.control;

import qwirkle.event.GameThreadStatus;

/** Automatically run a game, pause it, etc. Posts itself to the EventBus whenever
 *  thread status changes. */
public class QwirkleThreads {
    private static final long DEFAULT_STEP_MILLIS = 1000;
    private static final long TICK_MILLIS = 50;

    private GameManager mgr;
    private long stepMillis = DEFAULT_STEP_MILLIS;
    private Filament filament;

    public QwirkleThreads(GameManager mgr) {
        if (mgr == null) throw new NullPointerException("game manager is null");
        this.mgr = mgr;
    }

    /** Is a game thread currently running?
     *  May continue for a little while after <tt>stop</tt> is called.
     *  You can wait for a {@link GameThreadStatus} event. */
    public synchronized boolean isRunning() { return filament != null; }

    public synchronized void go() {
        if (filament == null) {
            filament = new Filament();
            filament.start();
        }
        // these events are sloppy -- could get out of order since we're no longer synchronized
        mgr.getEventBus().post(new GameThreadStatus(true));
    }

    public synchronized void stop() {
        // avoid need to synchronize by not using member directly
        Filament tmp = filament;
        if (tmp != null)
            // Tell filament to finish up.
            // It will automatically delete itself when it actually finishes.
            tmp.done();
    }

    public long getStepMillis() { return stepMillis; }
    public void setStepMillis(long stepMillis) { this.stepMillis = stepMillis; }

    private class Filament extends Thread {
        private boolean going = false;

        @Override
        public void run() {
            try {
                going = true;
                while (going) {
                    long start = System.currentTimeMillis();
                    mgr.step();
                    if (!mgr.isFinished())
                        sleepFrom(start);
                }
            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                done();
                synchronized (QwirkleThreads.this) {
                    filament = null;
                }
                // events are sloppy -- could get out of order since we're not synced
                mgr.getEventBus().post(new GameThreadStatus(false));
            }
        }

        private void sleepFrom(long start) throws InterruptedException {
            // if stepMillis is changed, handle it right away
            // wake up to check for a change every TICK_MILLIS milliseconds
            long end = start + stepMillis;
            long remain = end - System.currentTimeMillis();
            while (remain > 0 && going) {
                sleep(Math.min(remain, TICK_MILLIS));
                remain = end - System.currentTimeMillis();
            }
        }

        /** This filament is terminating or should terminate.
         *  Doesn't need to be synchronized, since it isn't handled synchronously
         *  -- it's just a suggestion that this be the last iteration of the loop. */
        private void done() {
            going = false;
        }
    }
}
