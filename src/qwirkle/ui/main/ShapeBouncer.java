package qwirkle.ui.main;

import qwirkle.game.QwirkleColor;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirkleShape;
import qwirkle.ui.paint.QwirklePiecePainter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;

/** Bounce the shapes around inside. */
public class ShapeBouncer extends JPanel {
    public static final Random r = new Random();

    /** The pieces that are bouncing around. */
    private List<QwirklePiece> pieces = new ArrayList<>();
    /** Map of the pieces to their locations & speeds in the window (x & y are between 0 and 1). */
    private final Map<QwirklePiece, PieceInfo> pieceInfos = new HashMap<>();

    /** How many millis between updates? */
    long stepMillis = 20;

    /** The thread that powers updates. */
    UpdateThread updateThread;

    /** The area of this panel devoted to pieces. */
    private double totalAreaOfPieces = 0.6;

    /** How many seconds should a piece take (on average) to cross the window? */
    private double secondsToCross = 8; // 2.5;

    private double secondsToRotate = secondsToCross / 2;

    /** How big should the shapes be? Starts at 100, updates with size of frame,
     *  based on totalAreaOfPieces. */
    private double pieceSize = 100;

    private QwirklePiecePainter painter = new QwirklePiecePainter();

    private long lastUpdate = System.currentTimeMillis();

    public ShapeBouncer(QwirkleShape[] shapes, QwirkleColor[] colors) {
        int iColor = r.nextInt(colors.length);
        painter.setTransparency(0.5f);
        for (QwirkleShape shape : shapes)
            pieces.add(new QwirklePiece(colors[iColor++ % colors.length], shape));
        scatter();
        resume();

        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { updateScale(); }
            @Override public void componentShown(ComponentEvent e) { resume(); }
            @Override public void componentHidden(ComponentEvent e) { pause(); }
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        synchronized (pieceInfos) {
            for (QwirklePiece piece : pieces) {
                PieceInfo info = pieceInfos.get(piece);
                info.paint(g2, piece);
            }
        }
    }

    private void pause() {
        if (updateThread != null)
            updateThread.stopRunning();
        updateThread = null;
    }

    private void resume() {
        pause();
        lastUpdate = System.currentTimeMillis();
        updateThread = new UpdateThread();
        updateThread.start();
    }

    private void step() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastUpdate;
        if (elapsed > 0) {
            synchronized (pieceInfos) {
                for (QwirklePiece piece : pieces) {
                    PieceInfo info = pieceInfos.get(piece);
                    info.step(elapsed);
                }
            }
        }
        lastUpdate = now;
    }

    /** Dedicate about half of the space to pieces. */
    private void updateScale() {
        Dimension d = getSize();
        double areaPerPiece = totalAreaOfPieces * d.width * d.height / pieces.size();
        pieceSize = Math.sqrt(areaPerPiece);
    }

    /** Scatter the pieces randomly. */
    private void scatter() {
        synchronized (pieceInfos) {
            pieceInfos.clear();
            for (QwirklePiece piece : pieces) {
                pieceInfos.put(piece, new PieceInfo());
            }
            lastUpdate = System.currentTimeMillis();
        }
    }

    private class UpdateThread extends Thread {
        private boolean running = true;
        @Override
        public void run() {
            while(running) {
                long start = System.currentTimeMillis();
                step();
                long elapsed = System.currentTimeMillis() - start;
                repaint();
                try {
                    if (stepMillis > elapsed)
                        sleep(stepMillis - elapsed);
                } catch (InterruptedException ignored) {
                    stopRunning();
                }
            }
        }

        private void stopRunning() {
            running = false;
        }
    }

    private class PieceInfo {
        double px, py, vx, vy, pt, vt; // x, y, theta, all between 0 and 1

        PieceInfo() {
            px = r.nextDouble();
            py = r.nextDouble();
            pt = r.nextDouble();

            while (toSlow()) {
                vx = (2 * r.nextDouble() - 1) / secondsToCross;
                vy = (2 * r.nextDouble() - 1) / secondsToCross;
                vt = (2 * r.nextDouble() - 1) / secondsToRotate;
            }
        }

        // is the speed less than half the suggested speed
        // or the rotation less than a tenth?
        private boolean toSlow() {
            return vabs() < (0.5/secondsToCross)
                    || Math.abs(vt) < 0.1/secondsToRotate;
        }

        public void step(long millis) {
            px += vx * millis / 1000;
            py += vy * millis / 1000;
            pt += vt * millis / 1000;
            bounce();
        }

        private void bounce() {
            while (px < 0 || px > 1) {
                if (px < 0) {
                    reverseX();
                    px *= -1;
                }
                if (px > 1) {
                    reverseX();
                    px = 2-px;
                }
            }
            while (py < 0 || py > 1) {
                if (py < 0) {
                    reverseY();
                    py *= -1;
                }
                if (py > 1) {
                    reverseY();
                    py = 2-py;
                }
            }
            pt = pt % 1; // rotate
        }

        private void reverseX() { vx *= -1; reverseT(); }
        private void reverseY() { vy *= -1; reverseT(); }
        private void reverseT() { vt *= -1; }
        private double vabs() { return Math.sqrt(vabs2()); }
        private double vabs2() { return vx * vx + vy * vy; }

        public void paint(Graphics2D g2, QwirklePiece piece) {
            AffineTransform before = g2.getTransform();
            Dimension d = getSize();

            // translate piece location from 0-1 to 0-width (leaving a margin to show the piece)
            g2.translate(px * (d.width-pieceSize), py * (d.height-pieceSize));
            g2.scale(pieceSize/100, pieceSize/100);

            // rotation
            g2.translate(50, 50); // center
            g2.rotate(pt * 2 * Math.PI);
            g2.translate(-50, -50);

            // transparency: faint at edges
            double d4 = distanceFromEdge() * 4;
            double transparency = Math.max(0, 0.9 - d4);
            painter.setTransparency(transparency);

            // debug
//            g2.setColor(Color.GRAY);
//            g2.drawString(shorten("d " + d3), -20, -10);
//            g2.drawString(shorten("t " + transparency), -20, 90);

            painter.paint(g2, piece);
            g2.setTransform(before);
        }

        private String shorten(String s) {
            return s.length() > 10 ? s.substring(0, 10) : s;
        }

        private double distanceFromCenter() {
            return Math.sqrt((px-.5)*(px-.5) + (py-.5)*(py-.5));
        }
        private double distanceFromEdge() {
            return Math.min(distanceFromEdgeX(), distanceFromEdgeY());
        }
        private double distanceFromEdgeY() { return Math.min(py, 1-py); }
        private double distanceFromEdgeX() { return Math.min(px, 1-px); }
    }

    /** Pick one at random. */
    private <T> T pick(T[] ts) {
        return ts[r.nextInt(ts.length)];
    }
}
