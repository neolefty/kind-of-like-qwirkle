package qwirkle.ui.paint.colors;

/** A bunch of predefined color sets -- standard backgrounds. */
public class ColorSets {
    /** Normal background. */
    public static final ColorSet BG_NORMAL
            = new StaticColorSet(Colors.BG, Colors.MOUSE, Colors.CLICK);
    /** Background for something that is highlighted. */
    public static final ColorSet BG_HIGHLIGHT
            = new StaticColorSet(Colors.BG_HL, Colors.MOUSE_HL, Colors.CLICK_HL);

    public static final ColorSet BG_PLAYABLE
            = new StaticColorSet(Colors.PLAY_BG, Colors.PLAY_MOUSE, Colors.PLAY_HL);
}
