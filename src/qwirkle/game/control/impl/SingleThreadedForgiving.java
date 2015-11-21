package qwirkle.game.control.impl;

import qwirkle.game.control.ThreadingStrategy;

/** A degenerate threading strategy. Just runs things in the current thread.
 *  Prints out stack traces rather than propagating exceptions. */
public class SingleThreadedForgiving implements ThreadingStrategy {
    @Override
    public void execute(Runnable r) {
        try {
            r.run();
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }
}
