package qwirkle.ui.swing;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/** Monitors a component and keeps its font size proportional to another component. */
public class FontAutosizer {
    private double fraction = 0.2;
    private Component source, target;

    public FontAutosizer(Component target, Component source, double fraction) {
        this(target, source);
        setFractionOfMetric(fraction);
    }

    public static enum Metric { WIDTH, HEIGHT, MEAN }

    /** Width, height, or geometric mean? */
    private Metric metric = Metric.WIDTH;

    /** Monitor <tt>source</tt>'s size and update <tt>target</tt>'s size to match. */
    public FontAutosizer(Component target, Component source) {
        if (source == null)
            throw new NullPointerException("parent is null");
        this.source = source;
        this.target = target;
        source.addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) { updateSize(); }
            @Override public void componentResized(ComponentEvent e) { updateSize(); }
        });
    }

    /** What fraction of the metric should this be? For example, if the metric is WIDTH,
     * and this fraction is 0.15, then the font size of the target will be 0.15 the width of the source. */
    public void setFractionOfMetric(double fractionOfMetric) {
        this.fraction = fractionOfMetric;
        updateSize();
    }

    /** Get the measurement which we'll base our height on. */
    public float getMeasure() {
        switch (metric) {
            case WIDTH: return source.getWidth();
            case HEIGHT: return source.getHeight();
            case MEAN: return (float) Math.sqrt(source.getHeight() * source.getWidth());
            default: throw new IllegalArgumentException("Unknown metric: " + metric);
        }
    }

    public double getFractionOfMetric() { return fraction; }

    public void setMetric(Metric metric) {
        this.metric = metric;
        updateSize();
    }

    private void updateSize() {
        target.setFont(target.getFont().deriveFont((float) (fraction * getMeasure())));
    }
}
