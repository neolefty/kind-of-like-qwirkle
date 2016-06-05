package qwirkle.ui.swing.game;

import qwirkle.ui.UIConstants;
import qwirkle.ui.control.DiscardTracker;
import qwirkle.ui.swing.game.player.SwingPlayer;
import qwirkle.ui.swing.util.AutoSizeLabel;
import qwirkle.ui.swing.util.FontAutosizer;
import qwirkle.ui.view.HasAspectRatio;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

// TODO remove label and write discard in big grey letters across the panel that highlight when you grab a piece
// TODO add "Pass" button to pass with zero discards
/** A panel containing a DiscardGridPanel, handles layout etc. */
public class SwingDiscard extends JPanel implements HasAspectRatio {
    public static final String TEXT_DISCARD = "Discard";

    private SwingDiscardGrid gridPanel;
    private DiscardTracker controller;
    private AutoSizeLabel label;

    private Boolean vertical = null; // use an object to induce full layout first time

    public SwingDiscard(DiscardTracker controller) {
        this.controller = controller;
        gridPanel = new SwingDiscardGrid(controller);
        label = new AutoSizeLabel(this, TEXT_DISCARD, SwingPlayer.AUTO_SIZE_FRACTION);
        setLayout(new GridBagLayout());
        setVertical(true);
    }

    @Override
    public double getAspectRatio() {
        return SwingPlayer.getAspectRatio(vertical, controller.getNumSpots());
    }

    @Override
    public boolean isVertical() {
        return vertical; // initialized during constructor
    }

    // TODO why does this never get called?
    @Override
    public void paint(Graphics g) {
        // do the rest
        super.paint(g);
        // draw "discard" in big grey letters
        drawDiscardBg((Graphics2D) g);
    }

    private void drawDiscardBg(Graphics2D g) {
        if (vertical) {
            // capture state
            Font origFont = g.getFont();
            AffineTransform origTransform = g.getTransform();
            Color origColor = g.getColor();

            // draw
            Font big = (origFont.deriveFont((float) (vertical ? getWidth() : getHeight())));
            g.setFont(big);
            if (vertical) g.rotate(Math.PI / 2);
            g.setColor(new Color(UIConstants.BG_HL.getColorInt()));
            g.drawString(TEXT_DISCARD, 0, vertical ? getWidth() : getHeight());
            g.fillOval(0, 0, vertical ? getHeight() : getWidth(),
                    vertical ? getWidth() : getHeight());

            // restore state
            g.setFont(origFont);
            g.setTransform(origTransform);
            g.setColor(origColor);
        }
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
                label.setText(TEXT_DISCARD);
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
                label.setText(" " + TEXT_DISCARD);
                label.setHorizontalAlignment(SwingConstants.LEADING);
                add(label, constraints);
                constraints.gridy++;
            }
        }
    }
}
