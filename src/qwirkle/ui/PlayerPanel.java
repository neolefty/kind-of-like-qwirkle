package qwirkle.ui;

import com.google.common.eventbus.Subscribe;
import qwirkle.control.GameManager;
import qwirkle.control.GameStarted;
import qwirkle.game.QwirklePlayer;
import qwirkle.game.QwirkleTurn;
import qwirkle.ui.util.AutoSizeLabel;
import qwirkle.ui.util.FontAutosizer;
import qwirkle.ui.util.HasAspectRatio;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/** Show the current state of a player. Their hand, name, score, best play? */
public class PlayerPanel extends JPanel implements HasAspectRatio {
    /** If vertical, this is the ideal width-to-height ratio. */
    public static final double VERTICAL_ASPECT_RATIO = 1 / 7.5;
    /** If horizontal, this is the ideal width-to-height ratio. */
    public static final double HORIZONTAL_ASPECT_RATIO = 5;

    private PlayerHandPanel handPanel;
    // TODO adjust labels' font to fit? Maybe tie together sizes of all labels of same type (name & score, best move, etc)?
    private AutoSizeLabel nameLabel, scoreLabel, bestMoveLabel, scoreSeparatorLabel;
    private Boolean vertical = null;

    private Container labels = null;
    private Set<AutoSizeLabel> autoSizeLabels = new HashSet<>();

    public PlayerPanel(final GameManager mgr, final QwirklePlayer player) {
        this.handPanel = new PlayerHandPanel(mgr, player);

        setLayout(new GridBagLayout());
        double fraction = 0.25;
        nameLabel = new AutoSizeLabel(this, player.getName(), fraction);
        autoSizeLabels.add(nameLabel);
        scoreLabel = new AutoSizeLabel(this, "0", fraction);
        autoSizeLabels.add(scoreLabel);
        scoreSeparatorLabel = new AutoSizeLabel(this, ": ", fraction);
        autoSizeLabels.add(scoreSeparatorLabel);
        // TODO highlight best move on mouseover
        bestMoveLabel = new AutoSizeLabel(this, "", fraction * 0.7);
        autoSizeLabels.add(bestMoveLabel);
        setVertical(true);

        mgr.getEventBus().register(new Object() {
            @Subscribe public void turn(QwirkleTurn turn) {
                scoreLabel.setText("" + turn.getStatus().getScore(player));
                setBestMove(turn.getStatus().getAnnotatedGame().getBestTurn(player));
            }
            @Subscribe public void gameStart(GameStarted started) {
                clear();
            }
        });
    }

    @Override
    public double getAspectRatio() {
        return isHorizontal() ? HORIZONTAL_ASPECT_RATIO : VERTICAL_ASPECT_RATIO;
    }

    private void setBestMove(QwirkleTurn best) {
        if (best == null)
            bestMoveLabel.setText(" ");
        else {
            bestMoveLabel.setText("Best: " + best.getScore());
//            bestMoveLabel.setText("Best: " + best.getScore() + " for " + best.getPlacements().size() + " pieces");
        }
    }

    /** Clear state. */
    private void clear() {
        scoreLabel.setText("0");
        setBestMove(null);
    }

    @Override
    public void removeAll() {
        // remove grandchildren that we manage, if we have any
        if (labels != null) {
            labels.removeAll();
            labels = null;
        }
        super.removeAll();
    }

    public boolean isVertical() { return vertical; }
    public boolean isHorizontal() { return !isVertical(); }

    public void setVertical(boolean vertical) {
        if (this.vertical != null && this.vertical == vertical)
            return; // nop

        if (this.vertical == null)
            removeAll();

        this.vertical = vertical;

        GridBagConstraints constraints = new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0),
                0, 0);
        handPanel.setVertical(vertical);

        // vertical: top to bottom: name, score, hand panel, best move
        if (vertical) {
            for (AutoSizeLabel asl : autoSizeLabels)
                asl.setMetric(FontAutosizer.Metric.WIDTH);

            constraints.weighty = 0;
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(nameLabel, constraints);
            constraints.gridy++;

            constraints.weighty = 0;
            scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(scoreLabel, constraints);
            constraints.gridy++;

            constraints.weighty = 1;
            add(handPanel, constraints);
            constraints.gridy++;

            constraints.weighty = 0;
            bestMoveLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(bestMoveLabel, constraints);
            constraints.gridy++;
        }

        // horizontal: hand on top, labels underneath
        else {
            for (AutoSizeLabel asl : autoSizeLabels)
                asl.setMetric(FontAutosizer.Metric.HEIGHT);

            // hand on top, wide
            constraints.weighty = 1;
            constraints.gridwidth = 4;
            add(handPanel, constraints);
            constraints.gridy++;

            // labels underneath. Name: Score | best move
            labels = new Box(BoxLayout.X_AXIS);
            nameLabel.setHorizontalAlignment(SwingConstants.LEADING);
            labels.add(nameLabel);
            labels.add(scoreSeparatorLabel);
            scoreLabel.setHorizontalAlignment(SwingConstants.LEADING);
            labels.add(scoreLabel);
            labels.add(Box.createGlue());
            bestMoveLabel.setHorizontalAlignment(SwingConstants.LEADING);
            labels.add(bestMoveLabel);
            constraints.weighty = 0;
            add(labels, constraints);
        }
    }
}
