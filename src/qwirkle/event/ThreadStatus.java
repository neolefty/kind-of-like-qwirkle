package qwirkle.event;

/** Posted when the game is running on automatic or paused. */
public class ThreadStatus {
    private boolean running;

    public ThreadStatus(boolean running) { this.running = running; }

    public boolean isRunning() { return running; }

    @Override
    public String toString() {
        return "game threads " + (running ? "running" : "stopped");
    }
}
