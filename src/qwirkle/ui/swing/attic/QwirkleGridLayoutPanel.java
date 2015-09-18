package qwirkle.ui.swing.attic;

import com.google.common.eventbus.EventBus;
import qwirkle.game.QwirkleGrid;
import qwirkle.ui.swing.game.board.QwirklePiecePanel;
import qwirkle.ui.swing.paint.colors.Colors;

import java.awt.*;

/** Chopped up into a grid of QwirklePiecePanels. */
public class QwirkleGridLayoutPanel extends QwirkleGridSubscriberPanel {
    private EventBus bus;

    public QwirkleGridLayoutPanel(EventBus bus) {
        super(bus);
        this.bus = bus;
    }

    @Override
    public void onUpdate(QwirkleGrid grid) {
        int w = grid.getWidth(), h = grid.getHeight();
        int minX = grid.getXMin(), minY = grid.getYMin();
        removeAll();
        GridLayout layout = new GridLayout(0, w);
        setLayout(layout);
        layout.setHgap(0);
        layout.setVgap(0);
        for (int r = 0; r < h; ++r) {
            int y = r + minY;
            for (int c = 0; c < w; ++c) {
                int x = c + minX;
                add(new QwirklePiecePanel(bus, grid, x, y));
            }
        }
        validate();
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Colors.BG);
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paint(g);
    }
}
