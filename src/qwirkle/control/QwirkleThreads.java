package qwirkle.control;

import com.google.common.eventbus.Subscribe;
import qwirkle.event.GameOver;
import qwirkle.event.ThreadStatus;
import qwirkle.event.TurnStarting;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Automatically run a game, pause it, etc. Posts itself to the EventBus whenever
 *  thread status changes. */
public class QwirkleThreads {
    // how long to wait between normal steps
    private static final long DEFAULT_STEP_MILLIS = 1000;

    // how long to wait before giving up on a turn and stopping the autoplay
    private static final long DEFAULT_TURN_TIMEOUT = 10000;

    // how long to wait between games
    private static final long DEFAULT_GAME_OVER_MILLIS = 3000;

    // how often to check for an interrupt request
    private static final long TICK_MILLIS = 15;

    private GameController control;
    private long stepMillis = DEFAULT_STEP_MILLIS;
    private long gameOverMillis = DEFAULT_GAME_OVER_MILLIS;
    private long turnTimeout = DEFAULT_TURN_TIMEOUT;
    private Filament filament;

    private boolean autoRestart = true;

    /** Used like a reset-able CountDownLatch.
     *  Indicates when a turn has ended or a game started.
     *  The two threads waiting will be (1) The sleep thread waiting for our turn to end and
     *  (2) The event receiving thread (that doesn't actually wait).
     *  Discipline is important here: count down when a turn ends or when a game starts.
     *  Reset it when we're waiting for a turn to end or a game to start. */
    private final CyclicBarrier waitForTurn = new CyclicBarrier(2);

    /** Get ready to wait for a turn or new game start. (Note: returns immediately.) */
    private void resetWaitForTurn() {
        debugln(">>> Reset");
        waitForTurn.reset();
    }

    public QwirkleThreads(final GameController control) {
        if (control == null)
            throw new NullPointerException(GameController.class.getSimpleName() + " is null");
        this.control = control;
        control.register(new Object() {
            // these two things are what punctuate our steps and trigger us to act (once enough time has elapsed)
            // when a turn is starting, we can play
            @Subscribe public void turnStarting(TurnStarting event) { eventArrived(event); }
            // when a game ends, we can start a new one
            @Subscribe public void gameOver(GameOver event) { eventArrived(event); }
            // when a game begins, we can start taking turns
//            @Subscribe public void gameStarted(GameStarted event) { eventArrived(event); }
            private void eventArrived(Object event) {
                try {
                    debugln("\n" + control.getGame().getBoard().toString());
                    debugln("<<< Trigger next turn: " + event.toString());
                    waitForTurn.await(0, TimeUnit.NANOSECONDS);
                }
                catch (TimeoutException ignored) { } // we expect this
                catch(InterruptedException | BrokenBarrierException e) {
                    debugln("How many waiting now? " + waitForTurn.getNumberWaiting());
                    e.printStackTrace(System.out);
                }
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
    public void setStepMillis(long stepMillis) {
        this.stepMillis = stepMillis;
        warnTimeouts();
    }

    /** How long to wait for a player to take a turn before giving up and stopping automation.
     *  If negative, never time out. Default 10 sec.
     *  Note: If this is shorter than {@link #setStepMillis}, it will be ignored in favor of that instead. */
    public long getTurnTimeoutMillis() { return turnTimeout; }
    public void setTurnTimeoutMillis(long turnTimeout) {
        this.turnTimeout = turnTimeout;
        warnTimeouts();
    }

    private void warnTimeouts() {
        if (getTurnTimeoutMillis() < getStepMillis()) {
            debugln();
            System.out.println(getClass().getSimpleName()
                    + ": Warning: Turn timeout is shorter than game step time.");
        }
        if (getTurnTimeoutMillis() < getGameOverMillis()) {
            debugln();
            System.out.println(getClass().getSimpleName()
                    + ": Warning: Turn timeout is shorter than game over wait time.");
        }
    }

    // TODO make this actually work -- currently it uses stepMillis for everything
    /** How long to pause when a game ends before starting a new one (if autoplay is on). */
    public long getGameOverMillis() { return gameOverMillis; }
    public void setGameOverMillis(long gameOverMillis) {
        this.gameOverMillis = gameOverMillis;
        warnTimeouts();
    }

    private class Filament extends Thread {
        private boolean going = false;

        @Override
        public void run() {
            try {
                going = true;
                while (going) {
                    long start = System.currentTimeMillis();
                    resetWaitForTurn(); // set up the CyclicBarrier again
                    doTheNextThing();
//                    debugln("Game over? " + control.getGame().isFinished());
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

        // Take the next step in the game
        private void doTheNextThing() {
            // end of game --> either restart or break out of our loop
            if (control.getGame().isFinished()) {
                debugln("Game is finished:\n" + control.getGame().getBoard());
                if (!isAutoRestart())
                    done(); // stop if we aren't automatically restarting the game
                else
                    control.getGame().start(); // otherwise, restart the game
            }
            // if a game hasn't started yet, auto-start it
            else if (!control.getGame().isStarted())
                control.getGame().start();
                // game hasn't ended yet, so take a turn
            else
                control.getGame().step();
        }

        private void sleepFrom(long start) throws InterruptedException {
            debug("<<< waiting until current player finishes");
            try {
                // wait for the current player to finish
                if (turnTimeout < 0) { // is our patience infinite?
                    debugln("or forever (a day) ...");
                    waitForTurn.await(1, TimeUnit.DAYS);
                }
                else {
                    // timeout if we lose patience, or the next player is supposed to play, whichever is longer
                    // (no need to time out if it's not the end of the allowed turn yet)
                    // (by default, turnTimeout is longest, at 10 seconds)
                    long waitMillis = Math.max(turnTimeout, getStepMillis());
                    debugln("we'll time out after " + waitMillis + " ms ...");

                    // the barrier will break when it's the next player's turn or the game ends
                    // because of the other await(), which is triggered by events
                    waitForTurn.await(waitMillis, TimeUnit.MILLISECONDS);
                }
                // the other await() triggered *after* this one.
                // this means that the player probably took more time to think than the nominal time between turns
                debugln("  < player took a while to finish: " + (System.currentTimeMillis() - start) + " ms");
            } catch (BrokenBarrierException ignored) {
                // the other await() triggered *before* this one.
                // we expect this, if the time between turns is longer than players take to think
                debugln("  < player finished with time to spare: " + (System.currentTimeMillis() - start) + " ms");
            } catch (TimeoutException e) { // we've run out of patience, so stop auto-playing
                // this means that the player took a really long time, and we've run out of patience
                debugln("  < timeout waiting for player to finish: " + (System.currentTimeMillis() - start)
                        + " ms --> break out of autoplay loop");
                done(); // on timeout, break out of autoplay
                System.out.println("Autoplay timed out after " + turnTimeout + " ms.");
            }
            // if stepMillis is changed, handle it right away
            // wake up to check for a change every TICK_MILLIS milliseconds, for responsiveness
            long end = start + getWaitMillis();
            long remain = end - System.currentTimeMillis();
            debugln("  > Waiting to trigger next turn: " + remain + " ms"
                    + " (game over? " + control.getGame().isFinished() + ")");
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

    private static final boolean DEBUG = false;
    private transient long beginning = System.currentTimeMillis();
    private String ts() { return "-=[" + (System.currentTimeMillis() - beginning) + "]=-"; }
    private void debug(String s) { if (DEBUG) System.out.print(ts() + " " + s + " "); }
    private void debug() { debug(""); }
    private void debugln(String s) { if (DEBUG) System.out.println(ts() + " " + s); }
    private void debugln() { debugln(""); }
}
