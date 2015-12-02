package qwirkle.ui.control;

/** Disposes and undisposes based on UI changes. */
public interface DisposeUndisposer {
    interface Worker {
        void dispose();
        void undispose();
    }
    void register(Worker worker);
}
