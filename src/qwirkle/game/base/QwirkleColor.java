package qwirkle.game.base;

import java.util.*;

public class QwirkleColor {
    // note: if we initialize this here, it happens after
    private static final Map<String, QwirkleColor> abbrevMap = new HashMap<>();

    // darker or lighter
    private static final double GRADATION = 0.65;

    public static final QwirkleColor
            RED = new QwirkleColor(225, 20, 20, "red", "r"),
            ORANGE = new QwirkleColor(240, 130, 0, "orange", "o"),
            YELLOW_ORANGE = new QwirkleColor(255, 214, 0, "yellow-orange", "w"),
            YELLOW = new QwirkleColor(215, 255, 0, "yellow", "y"),
            GREEN = new QwirkleColor(0, 180, 70, "green", "g"),
            BLUE = new QwirkleColor(0, 60, 255, "blue", "b"),
            PURPLE = new QwirkleColor(140, 0, 255, "purple", "p"),
            GREY = new QwirkleColor(140, 140, 140, "grey", "e"),
            WHITE = new QwirkleColor(255, 255, 255, "white", "h"),
            BLACK = new QwirkleColor(0, 0, 0, "black", "k"),

            GREY_1 = WHITE,
            GREY_2 = new QwirkleColor(193, "grey 2", "2"),
            GREY_3 = new QwirkleColor(147, "grey 3", "3"),
            GREY_4 = new QwirkleColor(112, "grey 4", "4"),
            GREY_5 = new QwirkleColor(85, "grey 5", "5"),
            GREY_6 = new QwirkleColor(65, "grey 6", "6"),
            GREY_7 = new QwirkleColor(49, "grey 7", "7");

    public static Collection<QwirkleColor> values() {
        return Collections.unmodifiableCollection(abbrevMap.values());
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
    private String name, abbrev;

    /** No name or abbreviation. */
    private QwirkleColor(int r, int g, int b, String name) { this(r, g, b, name, null); }

    public static List<QwirkleColor> createRainbow(int n) { return createRainbow(n, 0, 1, 0.9); }

    public static List<QwirkleColor> createRainbow
            (int n, double startHue, double saturation, double brightness)
    {
        List<QwirkleColor> result = new ArrayList<>();
        double d = 1. / n;
        for (int i = 0; i < n; ++i)
            result.add(fromHSV(startHue + i * d, saturation, brightness));
        return Collections.unmodifiableList(result);
    }

    /** From hue, saturation, brightness. */
    private static QwirkleColor fromHSV(double h, double s, double v) {
        int[] rgb = hsvToRgb(h, s, v);
        return new QwirkleColor(rgb[0], rgb[1], rgb[2], null);
    }

    /** Grey -- use <tt>v</tt> as r, g, and b. */
    private QwirkleColor(int v, String name, String abbrev) {
        this(v, v, v, name, abbrev);
    }

    /** Grey -- use <tt>v</tt> as r, g, and b. */
    private QwirkleColor(int v, String name) {
        this(v, v, v, name, null);
    }

    /** Construct a new color and, if abbrev is non-null, add it to the map of abbreviations. */
    private QwirkleColor(int r, int g, int b, String name, String abbrev) {
        this.r = r; this.g = g; this.b = b;
        this.color = b + (g << 8) + (r << 16);
        this.name = name;
        this.abbrev = abbrev;
        if (abbrev != null) {
            if (abbrev.length() != 1)
                throw new IllegalArgumentException("Abbreviation is not one character: \"" + abbrev + "\"");
            abbrevMap.put(abbrev, this);
        }
    }

    /** The RGB color 0xRRGGBB. Avoid dependency on java.awt.Color. */
    public int getColorInt() { return color; }

    public String getAbbrev() { return abbrev; }

    public int getR() { return r; }
    public int getG() { return g; }
    public int getB() { return b; }

    private QwirkleColor brighter = null;
    public QwirkleColor brighter() {
        if (brighter == null) {
            String newName = "bright " + name;
            // modified from java.awt.Color.brighter()
            int newR = r, newG = g, newB = b;
            int i = (int) (1. / (1. - GRADATION));
            if (r == 0 && g == 0 && b == 0)
                brighter = new QwirkleColor(i, newName);
            else {
                newR = Math.min((int) (Math.max(newR, i) / GRADATION), 255);
                newG = Math.min((int) (Math.max(newG, i) / GRADATION), 255);
                newB = Math.min((int) (Math.max(newB, i) / GRADATION), 255);
                brighter = new QwirkleColor(newR, newG, newB, "bright " + name);
            }
        }
        return brighter;
    }

    private QwirkleColor darker = null;
    public QwirkleColor darker() {
        if (darker == null)
            darker = new QwirkleColor
                    ((int) (r * GRADATION), (int) (g * GRADATION), (int) (b * GRADATION), "dark " + name);
        return  darker;
    }

    /** Find a shape by abbreviation. */
    public static QwirkleColor pick(String abbrev) {
        QwirkleColor result = abbrevMap.get(abbrev);
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

    @Override public String toString() { return (name == null ? "(" + r + "," + g + "," + b + ")" : name); }

    // adapted from java.awt.Color
    public static int[] hsvToRgb(double h, double s, double v) {
        // saturation is zero -> grey
        if (s == 0) {
            int x = (int) (v * 255.0 + 0.5);
            return new int[] { x, x, x };
        } else {
            int r = -1, g = -1, b = -1;
            double rainbow = (h - Math.floor(h)) * 6.0;
            double f = rainbow - Math.floor(rainbow);
            double p = v * (1. - s);
            double q = v * (1. - s * f);
            double t = v * (1. - (s * (1. - f)));
            switch ((int) rainbow) {
                case 0:
                    r = (int) (v * 255. + 0.5);
                    g = (int) (t * 255. + 0.5);
                    b = (int) (p * 255. + 0.5);
                    break;
                case 1:
                    r = (int) (q * 255. + 0.5);
                    g = (int) (v * 255. + 0.5);
                    b = (int) (p * 255. + 0.5);
                    break;
                case 2:
                    r = (int) (p * 255. + 0.5);
                    g = (int) (v * 255. + 0.5);
                    b = (int) (t * 255. + 0.5);
                    break;
                case 3:
                    r = (int) (p * 255. + 0.5);
                    g = (int) (q * 255. + 0.5);
                    b = (int) (v * 255. + 0.5);
                    break;
                case 4:
                    r = (int) (t * 255. + 0.5);
                    g = (int) (p * 255. + 0.5);
                    b = (int) (v * 255. + 0.5);
                    break;
                case 5:
                    r = (int) (v * 255. + 0.5);
                    g = (int) (p * 255. + 0.5);
                    b = (int) (q * 255. + 0.5);
                    break;
            }
            return new int[] { r, g, b };
        }
    }
}
