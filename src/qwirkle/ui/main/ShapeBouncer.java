package qwirkle.ui.main;

import qwirkle.control.GameManager;
import qwirkle.game.QwirkleColor;
import qwirkle.game.QwirklePiece;
import qwirkle.game.QwirkleSettings;
import qwirkle.game.QwirkleShape;
import qwirkle.ui.paint.QwirklePiecePainter;
import qwirkle.ui.util.SwingKitty;
import qwirkle.ui.util.SwingSetup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;

/** Bounce the shapes around inside. */
public class ShapeBouncer extends JPanel {
    /** How many seconds should a piece take (on average) to cross the window? */
    private double secondsToCross = 12; // 2.5;

    /** How many seconds should it take for a piece to complete a rotation? */
    private double secondsToRotate = secondsToCross / 2;

    /** How many millis between updates? */
    private long stepMillis = 16;

    /** The thread that powers updates. */
    private UpdateThread updateThread;

    /** The area of this panel devoted to pieces. */
    private double totalAreaOfPieces = 0.8;

    private boolean resetOnResume = false;

    /** How close to the edge of the window do the pieces get before they turn transparent?
     *  2 for always somewhat transparent, higher for closer to the edge. Default 10. */
    private double transparency = 10;

    private boolean changeColors = true;

    /** The pieces that are bouncing around. */
    private List<QwirklePiece> pieces = new ArrayList<>();

    public static final Random r = new Random();

    private QwirkleColor[] colors;

    /** Map of the pieces to their locations & speeds in the window (x & y are between 0 and 1). */
    private final Map<QwirklePiece, PieceInfo> pieceInfos = new HashMap<>();

    /** How big should the shapes be? Starts at 100, updates with size of frame,
     *  based on totalAreaOfPieces. */
    private double pieceSize = 100;

    private QwirklePiecePainter painter = new QwirklePiecePainter();

    private long lastUpdate = System.currentTimeMillis();

    public ShapeBouncer(GameManager game) {
        // TODO: move this to scatter to keep it up to date with the game
        this(generatePieces(game));
    }

    public ShapeBouncer(QwirkleShape[] shapes, QwirkleColor[] colors) {
        this(generatePieces(shapes, colors));
    }

    public ShapeBouncer(Collection<QwirklePiece> pieces) {
        Set<QwirkleColor> colorsScratch = new HashSet<>();
        for (QwirklePiece piece : pieces) colorsScratch.add(piece.getColor());
        this.colors = colorsScratch.toArray(new QwirkleColor[colorsScratch.size()]);
        painter.setTransparency(0.5f);
        this.pieces.addAll(pieces);
        scatter();
        resume();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateScale();
            }

            @Override
            public void componentShown(ComponentEvent e) {
//                System.out.println("Bouncer is shown.");
                resume();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
//                System.out.println("Hidden bouncer is.");
                pause();
            }
        });
    }

    /** How many millis between screen updates? */
    public void setStepMillis(long stepMillis) {
        this.stepMillis = stepMillis;
    }

    private static Collection<QwirklePiece> generatePieces(GameManager game) {
        QwirkleSettings settings = game.getSettings();
        QwirkleSettings oneDeck = new QwirkleSettings
                (1, settings.getShapes(), settings.getColors(), settings.getPlayers());
        return oneDeck.generate();
    }
    private static Collection<QwirklePiece> generatePieces(QwirkleShape[] shapes, QwirkleColor[] colors) {
        int iColor = r.nextInt(colors.length);
        List<QwirklePiece> pieces = new ArrayList<>();
        for (QwirkleShape shape : shapes)
            pieces.add(new QwirklePiece(colors[iColor++ % colors.length], shape));
        return pieces;
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
        if (resetOnResume)
            scatter();
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

    /** Scatter the pieces randomly to the edges of the screen. */
    private void scatter() {
        synchronized (pieceInfos) {
            pieceInfos.clear();
            for (QwirklePiece piece : pieces) {
                pieceInfos.put(piece, new PieceInfo());
            }
            lastUpdate = System.currentTimeMillis();
        }
    }

    /** How close to the edge of the window do the pieces get before they turn transparent?
     *  2 for always somewhat transparent, higher for closer to the edge. Default 10. */
    public void setTransparency(int transparency) {
        this.transparency = transparency;
    }

    /** How many seconds does the fastest piece take to cross the screen?
     *  The slowest piece will move no more slowly than half this velocity
     *  (pythagorean sum of x and y speed). */
    public void setSecondsToCross(int secondsToCross) {
        this.secondsToCross = secondsToCross;
    }

    /** How many seconds does the fastest piece take to complete a rotation?
     *  The slowest spinner will turn no less than 1/10 this rate. */
    public void setSecondsToRotate(int secondsToRotate) {
        this.secondsToRotate = secondsToRotate;
    }

    public void setResetOnResume(boolean resetOnResume) {
        this.resetOnResume = resetOnResume;
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
        QwirkleColor color;

        PieceInfo() {
            // start on an edge
            if (r.nextBoolean()) { // on left or right edge
                px = (r.nextBoolean() ? 0 : 1);
                py = r.nextDouble();
            } else { // top or bottom edge
                px = r.nextDouble();
                py = (r.nextBoolean() ? 0 : 1);
            }

            // with a random orientation
            pt = r.nextDouble();

            // with a random color
            changeColor();

            while (tooSlow()) {
                vx = (2 * r.nextDouble() - 1);
                vy = (2 * r.nextDouble() - 1);
                vt = (2 * r.nextDouble() - 1);
            }
        }

        // is the speed less than half the suggested speed
        // or the rotation less than a tenth?
        private boolean tooSlow() {
            return vabs() < (0.5)
                    || Math.abs(vt) < 0.1;
        }

        public void step(long millis) {
            px += vx * millis / secondsToCross / 1000;
            py += vy * millis / secondsToCross / 1000;
            pt += vt * millis / secondsToRotate / 1000;
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
        private void reverseT() { vt *= -1; changeColor(); }
        private void changeColor() { color = pick(colors); }

        private double vabs() { return Math.sqrt(vabs2()); }
        private double vabs2() { return vx * vx + vy * vy; }

        public void paint(Graphics2D g2, QwirklePiece piece) {
            AffineTransform before = g2.getTransform();
            Dimension dim = getSize();

            // IDEA: bounce off edge of screen instead? Then color change would occur off-stage
            // translate piece location from 0-1 to 0-width (leaving a margin to show the piece)
            g2.translate(px * (dim.width-pieceSize), py * (dim.height-pieceSize));
            g2.scale(pieceSize/100, pieceSize/100);

            // rotation
            g2.translate(50, 50); // center
            g2.rotate(pt * 2 * Math.PI);
            g2.translate(-50, -50);

            // IDEA: start color change cycle at bounce? Bounce -> fade out -> change -> fade in

            // transparency: faint at edges
            double dist = distanceFromEdge() * transparency;
            // don't fade all the way -- bounce & switch colors at 95% transparency
            double transparency = Math.max(0, 0.95 - dist);
            painter.setTransparency(transparency);

            // debug
//            g2.setColor(Color.GRAY);
//            g2.drawString(shorten("d " + d3), -20, -10);
//            g2.drawString(shorten("t " + transparency), -20, 90);

            if (changeColors)
                piece = new QwirklePiece(color, piece.getShape());
            painter.paint(g2, piece);
            g2.setTransform(before);
        }

//        private String shorten(String s) { return s.length() > 10 ? s.substring(0, 10) : s; }
//        private double distanceFromCenter() { return Math.sqrt((px-.5)*(px-.5) + (py-.5)*(py-.5)); }

        private double distanceFromEdge() {
            return Math.min(distanceFromEdgeX(), distanceFromEdgeY());
        }
        private double distanceFromEdgeY() { return Math.min(py, 1-py); }
        private double distanceFromEdgeX() { return Math.min(px, 1-px); }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("All The Shapes!");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        SwingSetup.addWindowSizer(frame, ShapeBouncer.class);

        boolean allShapes = false;

        ShapeBouncer bouncer;
        if (allShapes) {
            int copies = 3;
            List<QwirkleShape> shapes = new ArrayList<>();
            for (int i = 0; i < copies; ++i)
                shapes.addAll(Arrays.asList(QwirkleShape.values()));
            QwirkleShape[] shapesArray = shapes.toArray(new QwirkleShape[shapes.size()]);
            bouncer = new ShapeBouncer(shapesArray, QwirkleColor.values());
        }
        else {
            bouncer = new ShapeBouncer(new GameManager(new QwirkleSettings()));
            bouncer.changeColors = false;
        }
        frame.setContentPane(bouncer);
        SwingKitty.setColors(bouncer);

        frame.setVisible(true);
    }

    /** Pick one at random. */
    private <T> T pick(T[] ts) {
        return ts[r.nextInt(ts.length)];
    }
}