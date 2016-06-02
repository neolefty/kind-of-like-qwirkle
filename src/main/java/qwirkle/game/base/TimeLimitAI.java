package qwirkle.game.base;

import qwirkle.util.Stopwatch;

/** A Qwirkle AI that has a time limit per turn. */
public abstract class TimeLimitAI implements QwirkleAI {
    private Stopwatch lastMoveWatch;
    private long maxMillis = -1;

    /** Get a stopwatch that recorded the phases of the most recent move. */
    public Stopwatch getLastMoveWatch() { return lastMoveWatch; }

    protected void setLastMoveWatch(Stopwatch lastMoveWatch) {
        this.lastMoveWatch = lastMoveWatch;
    }

    /** Maximum milliseconds to take to generate a turn.
     *  Approximate -- may exceed slightly.
     *  If non-positive, no limit (default -1). */
    public long getMaxMillis() { return maxMillis; }

    public void setMaxMillis(long maxMillis) { this.maxMillis = maxMillis; }
}
