package qwirkle.ui.swing.util;

import qwirkle.ui.view.HasAspectRatio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Random;

/** A panel with a square sub-panel at the left/top end. */
public class SquareEndPanel extends JPanel implements HasAspectRatio {
    private Component square;
    private HasAspectRatio theRest;
    private boolean vertical = true;

    /** Create a new panel.
     *  @param square a child that will be kept square, on the left/top end.
     *  @param theRest also a Component. The other part of this panel. */
    public SquareEndPanel(Component square, HasAspectRatio theRest) {
        this.square = square;
        this.theRest = theRest;
        setLayout(null);
        add(square);
        add((Component) theRest);
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { redraw(); }
            @Override public void componentShown(ComponentEvent e) { redraw(); }
        });
    }

    @Override
    public void setVertical(boolean vertical) {
        theRest.setVertical(vertical);
        if (this.vertical != vertical) {
            this.vertical = vertical;
            redraw();
        }
    }

    @Override
    public double getAspectRatio() {
        double r = theRest.getAspectRatio();
        return isVertical() ? 1. / (1/r + 1) : r + 1;
    }

    @Override
    public boolean isVertical() { return vertical; }

    private void redraw() {
        int squareSide = vertical ? getWidth() : getHeight();
        square.setBounds(0, 0, squareSide, squareSide);
        if (vertical)
            ((Component) theRest).setBounds(0, squareSide, getWidth(), getHeight() - squareSide);
//            ((Component) theRest).setBounds(0, squareSide + 1, getWidth(), getHeight() - squareSide - 1);
        else
            ((Component) theRest).setBounds(squareSide, 0, getWidth() - squareSide, getHeight());
//            ((Component) theRest).setBounds(squareSide + 1, 0, getWidth() - squareSide - 1, getHeight());
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Square End");
        JLabel square = new JLabel("square", JLabel.CENTER);
        square.setOpaque(true);
        square.setBackground(Color.YELLOW); square.setForeground(Color.BLACK);
        class TestTheRest extends JLabel implements HasAspectRatio {
            TestTheRest() {
                super("the rest", JLabel.CENTER);
                setBackground(Color.BLACK); setForeground(Color.YELLOW);
                setOpaque(true);
            }
            private boolean vertical = false;
            @Override public double getAspectRatio() { return 1; } // don't really care
            @Override public boolean isVertical() { return vertical; }
            @Override public void setVertical(boolean vertical) { this.vertical = vertical; }
        }
        final SquareEndPanel panel = new SquareEndPanel(square, new TestTheRest());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(panel);
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                panel.setVertical(panel.getHeight() > panel.getWidth());
            }
        });
        Random r = new Random();
        frame.setSize(200 + r.nextInt(500), 200 + r.nextInt(500));
        frame.setVisible(true);
    }
}
