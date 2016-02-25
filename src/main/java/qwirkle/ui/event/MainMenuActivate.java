package qwirkle.ui.event;

/** The main menu is being requested or dismissed. */
public class MainMenuActivate {
    private final boolean visible;

    public MainMenuActivate(boolean visible) { this.visible = visible; }

    public boolean isVisible() { return visible; }
}
