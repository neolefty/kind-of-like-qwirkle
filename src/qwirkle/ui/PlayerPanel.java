package qwirkle.ui;

import com.google.common.eventbus.Subscribe;
import qwirkle.control.GameManager;
import qwirkle.control.GameStarted;
import qwirkle.game.QwirklePlayer;
import qwirkle.game.QwirkleTurn;

import javax.swing.*;
import java.awt.*;

/** Show the current state of a player. Their hand, name, score, best play? */
public class PlayerPanel extends JPanel {
    private PlayerHandPanel handPanel;
    // TODO adjust labels' font to fit? Maybe tie together sizes of all labels of same type (name & score, best move, etc)?
    private JLabel nameLabel, scoreLabel, bestMoveLabel, scoreSeparatorLabel;
    private Boolean vertical = null;

    private Container labels = null;

    public PlayerPanel(final GameManager mgr, final QwirklePlayer player) {
        this.handPanel = new PlayerHandPanel(mgr, player);

        setLayout(new GridBagLayout());
        nameLabel = new JLabel(player.getName());
        scoreLabel = new JLabel("0");
        scoreSeparatorLabel = new JLabel(": ");
        // TODO highlight best move on mouseover
        bestMoveLabel = new JLabel();
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

    private void setBestMove(QwirkleTurn best) {
        if (best == null)
            bestMoveLabel.setText("");
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
            constraints.weighty = 0;
            add(nameLabel, constraints);
            constraints.gridy++;

            constraints.weighty = 0;
            add(scoreLabel, constraints);
            constraints.gridy++;

            constraints.weighty = 1;
            add(handPanel, constraints);
            constraints.gridy++;

            constraints.weighty = 0;
            add(bestMoveLabel, constraints);
            constraints.gridy++;
        }

        // horizontal: hand on top, labels underneath
        else {
            // hand on top, wide
            constraints.weighty = 1;
            constraints.gridwidth = 4;
            add(handPanel, constraints);
            constraints.gridy++;

            // labels underneath. Name: Score | best move
            labels = new Box(BoxLayout.X_AXIS);
            labels.add(nameLabel);
            labels.add(scoreSeparatorLabel);
            labels.add(scoreLabel);
            labels.add(Box.createGlue());
            labels.add(bestMoveLabel);
            constraints.weighty = 0;
            add(labels, constraints);
        }
    }
}
