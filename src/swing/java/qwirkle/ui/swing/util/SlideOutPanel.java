package qwirkle.ui.swing.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/** A Panel that has a slide-out menu thingy on the left, like Android or iOS. */
public class  SlideOutPanel<MainT extends Component, SlideT extends Component> extends JLayeredPane {
    private Timer slideTimer;

    /** How fast is the slide currently moving, in window-widths per animTime? */
    private double slideVelocity = 0;

    private long animTime = 500; // millis to complete an animation

    /** Where should the slide sit when it is fully visible? */
    private double slideProportion = 0.6;
//    private int slideMin = 0, slideMax = -1;

    private MainT main;
    private SlideT slide;

    /** Where is the slide now? 0 = hidden, 1 = fully covering the main panel. */
    private double slidePosition = 0;

    public SlideOutPanel() {
        this(null, null);
    }

    public SlideOutPanel(final MainT main, SlideT slide) {
        this.main = main;
        this.slide = slide;
        setLayout(null);
        setSlideComp(slide);
        setMainComp(main);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateMainPos();
                updateSlidePos();
            }
        });
        updateMainPos();
        updateSlidePos();
    }

    public void setMainComp(MainT main) {
        if (this.main != null)
            remove(this.main);
        this.main = main;
        if (main != null)
            add(main, new Integer(0));
        updateMainPos();
    }

    public MainT getMainComp() { return main; }

    public void setSlideComp(SlideT slide) {
        if (this.slide != null)
            remove(this.slide);
        this.slide = slide;
        if (slide != null)
            add(slide, new Integer(1));
        updateSlidePos();
    }

    public SlideT getSlideComp() { return slide; }

    private void updateSlidePos() {
        if (slide != null) {
            int slideRight = (int) (getWidth() * slidePosition); // slide right edge
            int slideWidth = (int) (slideProportion * getWidth()); // slide panel width (stays constant)
            slide.setBounds(slideRight - slideWidth, 0, slideWidth, getHeight());
            //        System.out.println(" --> " + (slideRight - slideWidth) + ", " + 0 + ", " + slideWidth + ", " + getHeight());
        }
    }

    private void updateMainPos() {
        if (main != null)
            main.setBounds(0, 0, getWidth(), getHeight());
    }

    /** Slide out or un-slide. */
    public synchronized void setSlideVisible(final boolean slideOut) {
        if (slideTimer != null) {
            slideTimer.stop();
            slideTimer = null;
        }
        final long start = System.currentTimeMillis();
        double slideDest = slideOut ? slideProportion : 0;
        final AnimFunction f = new AnimFunction(slidePosition, slideVelocity, slideDest, 0);
        slideTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long elapsed = System.currentTimeMillis() - start;
                double t = ((double) elapsed) / animTime; // t goes from 0 to 1
                if (t >= 1) {
                    slidePosition = f.getX(1);
                    slideTimer.stop();
                    slideTimer = null;
                }
                else
                    slidePosition = f.getX(t);
//                System.out.println(slidePosition);
                updateSlidePos();
            }
        });
        slideTimer.start();
    }

    private static class AnimFunction {
        /** Initial and final positions and velocities. At time 1, position will be xf. */
        private double x0, v0, xf, vf;

        public AnimFunction(double x0, double v0, double xf, double vf) {
            this.x0 = x0; this.v0 = v0; this.xf = xf; this.vf = vf;
        }

        // TODO make this a smooth function with momentum rather than linear movement
        public double getV(double t) {
            return xf - x0;
        }

        public double getX(double t) {
            return (xf - x0) * t + x0;
        }
    }

    public static void main(String[] args) {
        JButton show = new JButton("show");
        JButton hide = new JButton("hide");
        hide.setBackground(Color.BLACK); hide.setForeground(Color.YELLOW);
        show.setBackground(Color.YELLOW); show.setForeground(Color.BLACK);

        final JFrame frame = new JFrame("slide");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        final SlideOutPanel slide = new SlideOutPanel<>(show, hide);
        show.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { slide.setSlideVisible(true); }
        });
        hide.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { slide.setSlideVisible(false); }
        });
        frame.setContentPane(slide);
        frame.setVisible(true);
    }
}
