package qwirkle.ui.swing.colors;

import qwirkle.game.QwirkleColor;

import java.awt.*;

/** Gives a complimentary color, say for a background. */
public class ComplimentaryColor implements ColorSource {
    private QwirkleColor fg;

    public static final float DEFAULT_BRIGHTNESS = 0.2f, DEFAULT_SATURATION = 0.8f;

    private float brightness = DEFAULT_BRIGHTNESS;
    private float saturation = DEFAULT_SATURATION;

    /** Default dark background */
    public ComplimentaryColor() { }

    /** Custom background */
    public ComplimentaryColor(float brightness, float saturation) {
        this.brightness = brightness;
        this.saturation = saturation;
    }

    @Override
    public Color getColor() {
        float[] hsv = new float[3];
        Color c = fg.getColor();
        Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsv);
        hsv[0] += 0.5f; // complementary
        hsv[2] = brightness;
        hsv[1] = saturation;
        return Color.getHSBColor(hsv[0], hsv[1], hsv[2]);
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }
}
