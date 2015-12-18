package qwirkle.ui.swing.util;

import com.google.common.eventbus.EventBus;

import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

/** If the user is quiescent (no keyboard or mouse) for a certain amount of time, fire an event. */
public class SwingUserActivityTimeout {
    private EventBus bus;
    // true if we last fired a timeout event; false if we last fired a resume
    private boolean firedTimeout;
    private long lastActivity;
    private boolean running = false;
    private long timeoutMillis;
    private long resolutionMillis;

    private boolean debugging = false;

    private Set<Component> watched = new HashSet<>();

    // IDEA: also pass an event generator, but that's probably unnecessarily abstract for now

    /** Wait for the user to be quiescent by watching keyboard & mouse activity.
     *  @param watched the component to monitor
     *  @param bus where to receive & fire events
     *  @param timeoutMillis how long before firing an event
     *  @param resolutionMillis how often to check */
    public SwingUserActivityTimeout(Component watched, EventBus bus,
                                    final long timeoutMillis, final long resolutionMillis)
    {
        this.timeoutMillis = timeoutMillis;
        this.resolutionMillis = resolutionMillis;
        this.bus = bus;
        addWatched(watched);
        resume();
    }

    /** Fired when the user is quiescent long enough. */
    public class TimeoutEvent {
        private long elapsedMillis;
        private TimeoutEvent(long elapsedMillis) { this.elapsedMillis = elapsedMillis; }
        /** How long has it been since the last user activity? */
        public long getElapsedMillis() { return elapsedMillis; }
    }

    /** Fired when activity resumes. */
    public class ResumeEvent {
        private ResumeEvent() {}
    }

    public void setDebugging(boolean debugging) { this.debugging = debugging; }

    private KeyListener keys = new KeyAdapter() {
        @Override public void keyPressed(KeyEvent e) { activityDetected(); }
    };

    private MouseAdapter mouse = new MouseAdapter() {
        @Override public void mousePressed(MouseEvent e) { activityDetected(); }
        @Override public void mouseMoved(MouseEvent e) { activityDetected(); }
        @Override public void mouseWheelMoved(MouseWheelEvent e) { activityDetected(); }
    };

    /** Start paying attention (if not already doing it). */
    synchronized public boolean resume() {
        if (!running) {
            resetTimer();
            running = true;
            new Thread() {
                private long lastCheck = System.currentTimeMillis();
                public void run() {
                    while (running) {
                        try {
                            // have we timed out yet?
                            long elapsed = System.currentTimeMillis() - lastActivity;
                            debug("" + elapsed);
                            if (elapsed >= timeoutMillis)
                                timeout();

                            // check again after resolutionMillis
                            elapsed = System.currentTimeMillis() - lastCheck;
                            if (elapsed < resolutionMillis) {
                                sleep(resolutionMillis - elapsed);
                            }
                            lastCheck = System.currentTimeMillis();
                        } catch (InterruptedException ignored) { }
                    }
                }
            }.start();
            startWatching();
            return true;
        }
        else
            return false;
    }

    /** Is this currently active? */
    public boolean isRunning() { return running; }

    synchronized public void addWatched(Component component) {
        if (isRunning())
            stopWatching();
        watched.add(component);
        if (isRunning())
            startWatching();
        debug("started watching " + component);
    }

    /** Quit paying attention. */
    synchronized public boolean quit() {
        if (running) {
            running = false;
            stopWatching();
            return true;
        }
        else
            return false;
    }

    synchronized private void startWatching() {
        for (Component c : watched) {
            c.addKeyListener(keys);
            c.addMouseListener(mouse);
            c.addMouseMotionListener(mouse);
            c.addMouseWheelListener(mouse);
        }
    }

    synchronized private void stopWatching() {
        for (Component c : watched) {
            c.removeKeyListener(keys);
            c.removeMouseListener(mouse);
            c.removeMouseMotionListener(mouse);
            c.removeMouseWheelListener(mouse);
        }
    }

    /** Reset the timer. If we previously fired a timeout event, then fire a resume event. */
    synchronized public void activityDetected() {
        debug("*", false);
        if (firedTimeout) {
            debugln(); debug("reawakened");
            bus.post(new ResumeEvent());
            firedTimeout = false;
        }
        resetTimer();
    }

    /** Reset the activity timer to now, and watch for a timeout. */
    private void resetTimer() {
        firedTimeout = false;
        lastActivity = System.currentTimeMillis();
    }

    /** A timeout was detected. */
    synchronized public void timeout() {
        debug("#", false);
        if (!firedTimeout) {
            firedTimeout = true;
            long now = System.currentTimeMillis();
            debugln(); debug("Timed out after " + (now - lastActivity));
            bus.post(new TimeoutEvent(now - lastActivity));
        }
    }

    private transient boolean lineEnded = false;
    private void debug(String s, boolean sep) {
        if (debugging)
            System.out.print((lineEnded || !sep ? "" : " - ") + s);
        lineEnded = false;
    }
    private void debug(String s) {
        debug(s, true);
    }
    private void debugln() {
        if (debugging)
            System.out.println();
        lineEnded = true;
    }
}
