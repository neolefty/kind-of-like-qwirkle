package qwirkle.ui.board;

import qwirkle.game.*;
import qwirkle.ui.util.MouseSensitivePanel;
import qwirkle.ui.paint.QwirklePiecePainter;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class QwirklePiecePanel extends MouseSensitivePanel implements HasQwirkleLocation {
    private static final QwirklePiecePainter painter = new QwirklePiecePainter();
    private static final Color BG = Color.black,
            MOUSE = Color.GRAY.darker(), // Color.BLUE.darker(),
            CLICK = Color.GRAY; // Color.CYAN.darker();
    private static final Color BG_HL = Color.darkGray,
            MOUSE_HL = Color.GRAY, // Color.GREEN.darker(),
            CLICK_HL = Color.GRAY.brighter(); // Color.YELLOW.darker();

    private QwirklePiece piece;
    private QwirkleLocation location;

    public QwirklePiecePanel(QwirkleGrid grid, int x, int y) { this(grid, x, y, false); }

    public QwirklePiecePanel(QwirkleGrid grid, int x, int y, boolean highlight) {
        this(grid, new QwirkleLocation(x, y), highlight);
    }

    public QwirklePiecePanel(QwirkleGrid grid, QwirkleLocation location) { this(grid, location, false); }

    public QwirklePiecePanel(QwirkleGrid grid, QwirkleLocation location, boolean highlight) {
        this(location, highlight);
        piece = grid.get(location);
        if (piece != null)
            setToolTipText(new QwirklePlacement(piece, location).toString());
    }

    public QwirklePiecePanel(QwirklePlacement place) { this(place, false); }

    public QwirklePiecePanel(QwirklePlacement place, boolean highlight) {
        this(place.getLocation(), highlight);
        this.piece = place.getPiece();
        setToolTipText(piece.toString());
    }

    public QwirklePiecePanel(QwirkleLocation location) { this(location, false); }

    public QwirklePiecePanel(QwirkleLocation location, boolean highlight) {
        super(highlight ? BG_HL : BG, highlight ? MOUSE_HL : MOUSE, highlight ? CLICK_HL : CLICK);
        this.location = location;
        setToolTipText(location.toString());
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (piece != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            AffineTransform t = g2.getTransform();
            double xScale = getWidth() / 100., yScale = getHeight() / 100.;
            g2.scale(xScale, yScale);
            painter.paint((Graphics2D) g, new QwirklePlacement(piece, location));
            g2.setTransform(t);
        }
    }

    @Override
    public QwirkleLocation getQwirkleLocation() {
        return location;
    }

    @Override
    public String toString() {
        return location + ": " + (piece == null ? "blank" : piece);
    }
}
