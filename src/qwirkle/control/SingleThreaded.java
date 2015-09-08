package qwirkle.control;

/**
 * A degenerate threading strategy. Just runs things in the current thread.
 */
public class SingleThreaded implements ThreadingStrategy {
    @Override
    public void execute(Runnable r) {
        r.run();
    }
}
