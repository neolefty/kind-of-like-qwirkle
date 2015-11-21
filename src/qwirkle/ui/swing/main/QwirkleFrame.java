package qwirkle.ui.swing.main;

import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirkleSettings;
import qwirkle.ui.swing.paint.QwirklePiecePainter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

/** A frame with a Qwirkle icon. */
public class QwirkleFrame extends JFrame {
    public QwirkleFrame() {
        super("Qwirkle");
        setIconImage(createIconImage());
    }

    // choose a random Qwirkle shape & color and make it our icon
    private static Random r = new Random();
    private Image createIconImage() {
        // choose a piece randomly
        List<QwirklePiece> pieces = new QwirkleSettings(1).generate();
        QwirklePiece piece = pieces.get(r.nextInt(pieces.size()));
        int size = 100;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();
        double mag = 1.3;
        double translate = -(mag * size - size)/2;
        g.translate(translate, translate);
        g.scale(mag, mag);
        QwirklePiecePainter painter = new QwirklePiecePainter();
        painter.paint(g, piece);
        return image;
    }
}
