package qwirkle.ui.view;

/** Make something fade out over time. */
public interface Fader {
    void fade(long millis, Runnable callback);
}
