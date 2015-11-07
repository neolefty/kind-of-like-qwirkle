package qwirkle.control;

import com.google.common.eventbus.Subscribe;
import qwirkle.event.GameStarted;
import qwirkle.event.ThreadStatus;
import qwirkle.event.TurnCompleted;

import java.util.concurrent.CountDownLatch;

/** Automatically run a game, pause it, etc. Posts itself to the EventBus whenever
 *  thread status changes. */
public class QwirkleThreads {
    // how long to wait between normal steps
    private static final long DEFAULT_STEP_MILLIS = 1000;

    // how long to wait between games
    private static final long DEFAULT_GAME_OVER_MILLIS = 3000;

    // how often to check for an interrupt request
    private static final long TICK_MILLIS = 15;

    private GameController control;
    private long stepMillis = DEFAULT_STEP_MILLIS;
    private long gameOverMillis = DEFAULT_GAME_OVER_MILLIS;
    private Filament filament;

    private boolean autoRestart = true;
    // since things are threaded around here, we use this to know when a turn has ended
    private CountDownLatch waitForTurn;

    public QwirkleThreads(GameController control) {
        if (control == null)
            throw new NullPointerException(GameController.class.getSimpleName() + " is null");
        this.control = control;
        control.register(new Object() {
            @Subscribe public void turnCompleted(TurnCompleted event) { countDown(); }
            @Subscribe public void gameStarted(GameStarted event) { countDown(); }
            private void countDown() {
                if (waitForTurn != null)
                    waitForTurn.countDown();
            }
        });
    }

    /** Is a game thread currently running?
     *  May continue for a little while after <tt>stop</tt> is called.
     *  You can wait for a {@link ThreadStatus} event. */
    public synchronized boolean isRunning() { return filament != null; }

    public synchronized void go() {
        if (filament == null) {
            filament = new Filament();
            filament.start();
            control.post(new ThreadStatus(true));
        }
    }

    public synchronized void stop() {
        // avoid need to synchronize by not using member directly
        Filament tmp = filament;
        if (tmp != null)
            // Tell filament to finish up.
            // It will automatically delete itself when it actually finishes.
            tmp.done();
    }

    /** Should this automatically restart a game when it ends? Default true. */
    public boolean isAutoRestart() { return autoRestart; }

    /** Should this automatically restart a game when it ends? Default true. */
    public void setAutoRestart(boolean autoRestart) { this.autoRestart = autoRestart; }

    public long getStepMillis() { return stepMillis; }
    public void setStepMillis(long stepMillis) { this.stepMillis = stepMillis; }

    public long getGameOverMillis() { return gameOverMillis; }
    public void setGameOverMillis(long gameOverMillis) { this.gameOverMillis = gameOverMillis; }

    private class Filament extends Thread {
        private boolean going = false;

        @Override
        public void run() {
            try {
                going = true;
                while (going) {
                    long start = System.currentTimeMillis();
                    waitForTurn = new CountDownLatch(1);
                    // end of game --> either restart or stop running it
                    if (control.getGame().isFinished()) {
                        if (!isAutoRestart())
                            done();
                        else
                            control.getGame().start();
                    }
                    // game hasn't ended yet, so take a turn
                    else
                        control.getGame().step();
                    debugln("Done? " + control.getGame().isFinished());
                    // wait until it's time for the next turn
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
                control.post(new ThreadStatus(false));
            }
        }

        private void sleepFrom(long start) throws InterruptedException {
            debug("waiting ... ");
            long a = System.currentTimeMillis();
            waitForTurn.await();
            debugln(" got it " + (System.currentTimeMillis() - a));
            // if stepMillis is changed, handle it right away
            // wake up to check for a change every TICK_MILLIS milliseconds
            long end = start + getWaitMillis();
            long remain = end - System.currentTimeMillis();
            debugln("Waiting " + remain + " millis (" + control.getGame().isFinished() + ")");
            while (remain > 0 && going) {
                sleep(Math.min(remain, TICK_MILLIS));
                remain = end - System.currentTimeMillis();
            }
        }

        private long getWaitMillis() {
            return control.getGame().isFinished() ? gameOverMillis : stepMillis;
        }

        /** This filament is terminating or should terminate.
         *  Doesn't need to be synchronized, since it isn't handled synchronously
         *  -- it's just a suggestion that this be the last iteration of the loop. */
        private void done() {
            going = false;
        }
    }

    private static final boolean DEBUG = true;
    private void debug(String s) { if (DEBUG) System.out.print(s); }
    private void debugln(String s) { if (DEBUG) System.out.println(s); }
}
