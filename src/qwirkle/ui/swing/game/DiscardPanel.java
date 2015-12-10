package qwirkle.ui.swing.game;

import qwirkle.ui.control.DiscardTracker;
import qwirkle.ui.swing.game.player.PlayerPanel;
import qwirkle.ui.swing.util.AutoSizeLabel;
import qwirkle.ui.swing.util.FontAutosizer;
import qwirkle.ui.swing.util.HasAspectRatio;

import javax.swing.*;
import java.awt.*;

// TODO remove label and write discard in big grey letters across the panel that highlight when you grab a piece
// TODO add "Pass" button to pass with zero discards
/** A panel containing a DiscardGridPanel, handles layout etc. */
public class DiscardPanel extends JPanel implements HasAspectRatio {
    public static final String DISCARD = "Discard";

    private DiscardGridPanel gridPanel;
    private DiscardTracker controller;
    private AutoSizeLabel label;

    private Boolean vertical = null; // use an object to induce full layout first time

    public DiscardPanel(DiscardTracker controller) {
        this.controller = controller;
        gridPanel = new DiscardGridPanel(controller);
        label = new AutoSizeLabel(this, DISCARD, PlayerPanel.AUTO_SIZE_FRACTION);
        setLayout(new GridBagLayout());
        setVertical(true);
    }

    @Override
    public double getAspectRatio() {
        return PlayerPanel.getAspectRatio(vertical, controller.getNumSpots());
    }

    @Override
    public boolean isVertical() {
        return vertical; // initialized during constructor
    }

    public void setVertical(boolean vertical) {
        if (this.vertical == null || this.vertical != vertical) {
            removeAll();
            this.vertical = vertical;

            GridBagConstraints constraints = new GridBagConstraints(0, 0, 1, 1, 1, 1,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
                    0, 0);
            gridPanel.setVertical(vertical);

            if (vertical) {
                // label on top
                label.setMetric(FontAutosizer.Metric.WIDTH);
                label.setText(DISCARD);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                constraints.weighty = 0;
                add(label, constraints);
                constraints.gridy++;

                // discard area underneath
                constraints.weighty = 1;
                add(gridPanel, constraints);
                constraints.gridy++;

            } else {
                // discard area on top, wide
                constraints.weighty = 1;
                constraints.gridwidth = 4;
                add(gridPanel, constraints);
                constraints.gridy++;

                // label underneath
                constraints.weighty = 0;
                label.setMetric(FontAutosizer.Metric.HEIGHT);
                label.setText(" " + DISCARD);
                label.setHorizontalAlignment(SwingConstants.LEADING);
                add(label, constraints);
                constraints.gridy++;
            }
        }
    }
}
