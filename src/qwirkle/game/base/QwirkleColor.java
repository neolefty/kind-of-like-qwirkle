package qwirkle.game.base;

import java.util.*;

public class QwirkleColor {
    // note: if we initialize this here, it happens after
    private static final Map<String, QwirkleColor> values = new HashMap<>();

    // darker or lighter
    private static final double GRADATION = 0.65;

    public static final QwirkleColor
            RED = new QwirkleColor(225, 20, 20, "r"),
            ORANGE = new QwirkleColor(240, 130, 0, "o"),
            YELLOW_ORANGE = new QwirkleColor(255, 214, 0, "w"),
            YELLOW = new QwirkleColor(215, 255, 0, "y"),
            GREEN = new QwirkleColor(0, 180, 70, "g"),
            BLUE = new QwirkleColor(0, 60, 255, "b"),
            PURPLE = new QwirkleColor(140, 0, 255, "p"),
            GREY = new QwirkleColor(140, 140, 140, "e"),
            WHITE = new QwirkleColor(255, 255, 255, "h"),
            BLACK = new QwirkleColor(0, 0, 0, "k"),

            GREY_1 = WHITE,
            GREY_2 = new QwirkleColor(195, 195, 195, "2"),
            GREY_3 = new QwirkleColor(165, 165, 165, "3"),
            GREY_4 = new QwirkleColor(135, 135, 135, "4"),
            GREY_5 = new QwirkleColor(105, 105, 105, "5"),
            GREY_6 = new QwirkleColor(75, 75, 75, "6"),
            GREY_7 = new QwirkleColor(45, 45, 45, "7");

    public static Collection<QwirkleColor> values() {
        return Collections.unmodifiableCollection(values.values());
    }

    static public final List<QwirkleColor> EIGHT_COLORS
            = Collections.unmodifiableList(Arrays.asList(
            BLUE, GREEN, YELLOW, ORANGE, RED, PURPLE, YELLOW_ORANGE, GREY));

    static public final List<QwirkleColor> EIGHT_GREYS
            = Collections.unmodifiableList(Arrays.asList(
            WHITE, GREY_1, GREY_2, GREY_3, GREY_4, GREY_5, GREY_6, GREY_7));

    static public final List<QwirkleColor> SIX_GREYS
            = Collections.unmodifiableList(Arrays.asList(
            WHITE, GREY_1, GREY_2, GREY_3, GREY_4, GREY_5));

    static public final List<QwirkleColor> DEFAULT_COLORS
            = Collections.unmodifiableList(Arrays.asList(
            BLUE, GREEN, YELLOW, ORANGE, RED, PURPLE));

    static public final List<QwirkleColor> FIVE_COLORS
            = Collections.unmodifiableList(Arrays.asList(
            GREEN, YELLOW, ORANGE, RED, PURPLE));

    private final int color;
    private final int r, g, b;
    private String abbrev;

    /** Construct a new color and, if abbrev is non-null, add it to the values() collection. */
    private QwirkleColor(int r, int g, int b, String abbrev) {
        this.r = r; this.g = g; this.b = b;
        this.color = b + (g << 8) + (r << 16);
        this.abbrev = abbrev;
        if (abbrev != null)
            values.put(abbrev, this);
    }

    /** The RGB color 0xRRGGBB. Avoid dependency on java.awt.Color. */
    public int getColorInt() { return color; }

    public String getAbbrev() { return abbrev; }

    public int getR() { return r; }
    public int getG() { return g; }
    public int getB() { return b; }

    private QwirkleColor brighter = null;
    public QwirkleColor brighter() {
        if (darker == null) {
            // modified from java.awt.Color.brighter()
            int newR = r, newG = g, newB = b;
            int i = (int) (1. / (1. - GRADATION));
            if (r == 0 && g == 0 && b == 0)
                brighter = new QwirkleColor(i, i, i, null);
            else {
                newR = (int) (Math.max(newR, i) / GRADATION);
                newG = (int) (Math.max(newG, i) / GRADATION);
                newB = (int) (Math.max(newB, i) / GRADATION);
                brighter = new QwirkleColor(newR, newG, newB, null);
            }
        }
        return brighter;
    }

    private QwirkleColor darker = null;
    public QwirkleColor darker() {
        if (darker == null)
            darker = new QwirkleColor
                    ((int) (r * GRADATION), (int) (g * GRADATION), (int) (b * GRADATION), null);
        return  darker;
    }

    /** Find a shape by abbreviation. */
    public static QwirkleColor pick(String abbrev) {
        QwirkleColor result = values.get(abbrev);
        if (result == null)
            throw new IllegalArgumentException("no match for " + abbrev);
        return result;
    }

    /** Parse a sequence of single-character color abbreviations. */
    public static List<QwirkleColor> parseColors(String colors) {
        List<QwirkleColor> result = new ArrayList<>();
        for (int i = 0; i < colors.length(); ++i)
            result.add(pick(colors.substring(i, i+1)));
        return Collections.unmodifiableList(result);
    }
}
