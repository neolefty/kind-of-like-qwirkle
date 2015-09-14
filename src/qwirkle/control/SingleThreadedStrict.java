package qwirkle.control;

/** A degenerate threading strategy. Just runs things in the current thread.
 *  Doesn't catch exceptions -- good for testing but perhaps bad for production. */
public class SingleThreadedStrict implements ThreadingStrategy {
    @Override
    public void execute(Runnable r) {
        r.run();
    }
}