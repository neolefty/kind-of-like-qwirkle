package qwirkle.control;

/** A Threading strategy that creates a new thread each time. */
public class NewThreadEachTime implements ThreadingStrategy {
    @Override
    public void execute(final Runnable r) {
        new Thread() {
            @Override
            public void run() {
                // TODO deal with unhandled exceptions, especially IllegalStateException
                r.run();
            }
        }.run();
    }
}
