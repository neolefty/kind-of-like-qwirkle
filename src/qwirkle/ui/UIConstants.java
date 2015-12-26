package qwirkle.ui;

import qwirkle.game.base.QwirkleColor;

/** Constants for the UI. */
public class UIConstants {
    /** Production mode (true) or development mode (false)? */
    public static final boolean PRODUCTION = false;

    public static final long SCREENSAVER_TIMEOUT = 10 * 60 * 1000; // 10 minutes
//    public static final long SCREENSAVER_TIMEOUT = 3 * 1000; // 3 seconds
//    public static final long SCREENSAVER_TIMEOUT = 1000; // 1 second

    public static final String PREFS_WINDOW_LEFT = "Window Left";
    public static final String PREFS_WINDOW_TOP = "Window Top";
    public static final String PREFS_WINDOW_WIDTH = "Window Width";
    public static final String PREFS_WINDOW_HEIGHT = "Window Height";

    // Colors
    public static final QwirkleColor FG = QwirkleColor.WHITE; /** Text & border foreground */

    public static final QwirkleColor BG = QwirkleColor.BLACK; /** Normal background */
    public static final QwirkleColor MOUSE = QwirkleColor.GREY_6; /** Hover background */
    public static final QwirkleColor CLICK = QwirkleColor.GREY_5; /** Click background */

    public static final QwirkleColor BG_HL = QwirkleColor.GREY_6; /** Normal highlighted bg */
    public static final QwirkleColor MOUSE_HL = QwirkleColor.GREY_5; /** Hover highlighted bg */
    public static final QwirkleColor CLICK_HL = QwirkleColor.GREY_4; /** Click highlighted bg */
}
