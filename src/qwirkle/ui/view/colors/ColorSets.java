package qwirkle.ui.view.colors;

import qwirkle.ui.UIConstants;

/** A bunch of predefined color sets -- standard backgrounds. */
public class ColorSets {
    /** Normal background. */
    public static final ColorSet BG_NORMAL
            = new StaticColorSet(UIConstants.BG, UIConstants.MOUSE, UIConstants.CLICK);
    /** Background for something that is highlighted. */
    public static final ColorSet BG_HIGHLIGHT
            = new StaticColorSet(UIConstants.BG_HL, UIConstants.MOUSE_HL, UIConstants.CLICK_HL);
}
