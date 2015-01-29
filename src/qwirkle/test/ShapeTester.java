package qwirkle.test;

import qwirkle.game.QwirkleShape;
import qwirkle.ui.paint.QwirklePiecePainter;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class ShapeTester extends JPanel {
    private QwirkleShape shape;
    private double angle;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Shape Tester");
        Box outer = Box.createVerticalBox();
        Box inner1 = Box.createHorizontalBox();
        Box inner2 = Box.createHorizontalBox();
        outer.add(inner1); outer.add(inner2);
        frame.setContentPane(outer);
        inner1.add(new ShapeTester(QwirkleShape.flower, Math.PI, 20));
        inner1.add(new ShapeTester(QwirkleShape.smiley, Math.PI, 1000));
        inner1.add(new ShapeTester(QwirkleShape.flower, -Math.PI, 100));
        inner2.add(new ShapeTester(QwirkleShape.ay, -Math.PI, 200));
        inner2.add(new ShapeTester(QwirkleShape.square, Math.PI, 250));
        inner2.add(new ShapeTester(QwirkleShape.ay, Math.PI, 10));
//        frame.setContentPane(new ShapeTester(QwirkleShape.smiley));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setSize(900, 615);
    }

    class Rotator extends Thread {
        private final double speed;
        private final int interval;

        Rotator(double speed, int interval) {
            this.speed = speed;
            this.interval = interval;
        }

        @Override
        public void run() {
            while(true) {
                angle += interval * speed / 1000;
                if (angle > Math.PI * 2) {
                    angle -= Math.PI * 2;
                }
                repaint();
                try {
                    sleep(interval);
                } catch (InterruptedException ignored) { }
            }
        }
    }

    public ShapeTester(QwirkleShape shape, double speed, int interval) {
        this.shape = shape;
        new Rotator(speed, interval).start();
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        QwirklePiecePainter painter = new QwirklePiecePainter();
        g.setColor(Color.BLACK);

        g2.scale(getWidth() / 100., getHeight() / 100.);
        g2.translate(50, 50);
        g2.rotate(angle);
        g2.translate(-50, -50);

        painter.pickPainter(shape).paint(g2);

//        g.fillOval(0, 0, getWidth(), getHeight());
    }
}
