package qwirkle.ui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/** A label that adjusts its size based on its parent's width or height. */
public class AutoSizeLabel extends JLabel {
    /** Height of glyphs, compared to width or height. */
    private double fractionOfMetric = 0.2;
    private Component parent;

    public static enum Metric { WIDTH, HEIGHT, MEAN}

    /** Width, height, or geometric mean? */
    private Metric metric = Metric.WIDTH;

    public AutoSizeLabel(Component parent, String text, double fraction) {
        this(parent);
        setText(text);
        setFractionOfMetric(fraction);
    }

    public AutoSizeLabel(Component parent) {
        if (parent == null)
            throw new NullPointerException("parent is null");
        this.parent = parent;
        parent.addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) { updateSize(); }
            @Override public void componentResized(ComponentEvent e) { updateSize(); }
        });
    }

    private void updateSize() {
        setFont(getFont().deriveFont((float) (fractionOfMetric * getMeasure())));
    }

    public double getFractionOfMetric() {
        return fractionOfMetric;
    }

    public void setFractionOfMetric(double fractionOfMetric) {
        this.fractionOfMetric = fractionOfMetric;
        updateSize();
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    /** Get the measurement which we'll base our height on. */
    public float getMeasure() {
        switch (metric) {
            case WIDTH: return parent.getWidth();
            case HEIGHT: return parent.getHeight();
            case MEAN: return (float) Math.sqrt(parent.getHeight() * parent.getWidth());
            default: throw new IllegalArgumentException("Unknown metric: " + metric);
        }
    }
}
