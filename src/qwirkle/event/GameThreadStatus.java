package qwirkle.event;

/** Posted when the game is running on automatic or paused. */
public class GameThreadStatus {
    private boolean running;

    public GameThreadStatus(boolean running) { this.running = running; }

    public boolean isRunning() { return running; }
}
