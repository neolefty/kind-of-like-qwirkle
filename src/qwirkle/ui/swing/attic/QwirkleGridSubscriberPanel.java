package qwirkle.ui.swing.attic;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.game.QwirkleBoard;
import qwirkle.game.QwirkleGrid;

import javax.swing.*;

/** A panel that listens to the EventBus for {@link QwirkleBoard} and invalidates it receives one. */
public abstract class QwirkleGridSubscriberPanel extends JPanel {
    private QwirkleGrid grid;

    @Subscribe public void update(QwirkleGrid grid) {
        if (this.grid != grid) {
            this.grid = grid;
            onUpdate(grid);
            repaint();
        }
    }

    public QwirkleGridSubscriberPanel(EventBus bus) {
        bus.register(this);
    }

    public QwirkleGrid getGrid() { return grid; }

    /** Override this for convenience. */
    abstract public void onUpdate(QwirkleGrid grid);
}
