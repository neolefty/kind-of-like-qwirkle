package qwirkle.game.control;

/**
 * For asynchronous things (such as a human player taking a turn),
 * how should we handle threading? For example, during testing
 * it's simplest to keep it single-threaded, but when playing, you want
 * to be multi-threaded and get out of the Event thread
 * but maybe not have a bunch of threads hanging around?
 */
public interface ThreadingStrategy {
    void execute(Runnable r);
}
