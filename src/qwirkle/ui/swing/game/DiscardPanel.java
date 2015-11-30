package qwirkle.ui.swing.game;

import qwirkle.ui.control.QwirkleUIController;
import qwirkle.ui.swing.game.player.PlayerPanel;
import qwirkle.ui.swing.util.AutoSizeLabel;

import javax.swing.*;
import java.awt.*;

/** A panel containing a DiscardGridPanel, handles layout etc. */
public class DiscardPanel extends JPanel {
    private DiscardGridPanel gridPanel;
    private AutoSizeLabel label;

    private Boolean vertical = null; // use an object to induce full layout first time

    public DiscardPanel(QwirkleUIController controller) {
        gridPanel = new DiscardGridPanel(controller);
        label = new AutoSizeLabel(this, "Discard", PlayerPanel.AUTO_SIZE_FRACTION);
        setLayout(new GridBagLayout());
        setVertical(true);
    }

    public void setVertical(boolean vertical) {
        if (this.vertical != vertical) {
            removeAll();
            this.vertical = vertical;

            GridBagConstraints constraints = new GridBagConstraints(0, 0, 1, 1, 1, 1,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
                    0, 0);
            gridPanel.setVertical(vertical);
        }
    }
}
