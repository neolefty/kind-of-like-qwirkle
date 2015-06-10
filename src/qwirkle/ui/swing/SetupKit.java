package qwirkle.ui.swing;

import java.util.Random;

public class SetupKit {
    public static final String PREFS_WINDOW_LEFT = "Window Left",
            PREFS_WINDOW_TOP = "Window Top",
            PREFS_WINDOW_WIDTH = "Window Width",
            PREFS_WINDOW_HEIGHT = "Window Height";

    public static final Random r = new Random();

    /** A random number between <tt>min</tt> and <tt>max</tt>, inclusive. */
    public static int rand(int min, int max) {
        if (min > max)
            throw new IllegalArgumentException("Min (" + min + ") is greater than max (" + max + ").");
        else
            return min + r.nextInt((max - min) + 1);
    }

    /** A random real number between min and max, inclusive. */
    public static double rand(double min, double max) {
        if (min > max)
            throw new IllegalArgumentException("Min (" + min + ") is greater than max (" + max + ").");
        else
            return min + r.nextDouble() * (max - min);
    }
}
