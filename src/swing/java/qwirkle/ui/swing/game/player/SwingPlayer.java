package qwirkle.ui.swing.game.player;

import com.google.common.eventbus.Subscribe;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.control.AnnotatedGame;
import qwirkle.game.event.GameStarted;
import qwirkle.game.event.TurnCompleted;
import qwirkle.game.event.TurnStarting;
import qwirkle.ui.UIConstants;
import qwirkle.ui.control.QwirkleUIController;
import qwirkle.ui.control.SelfDisposingEventSubscriber;
import qwirkle.ui.swing.game.SwingTurnHighlightLabel;
import qwirkle.ui.swing.util.SwingPlatformAttacher;
import qwirkle.ui.swing.util.AutoSizeLabel;
import qwirkle.ui.swing.util.FontAutosizer;
import qwirkle.ui.view.HasAspectRatio;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

// TODO add coach option for human player ("Rainbow suggests ...")
/** Show the current state of a player. Their hand, name, score, best play so far in the game. */
public class SwingPlayer extends JPanel implements HasAspectRatio {
    /** How much extra vertical space, in units of Qwirkle piece grid squares,
     *  do we allow when in vertical mode, for labels? */
    public static final double VERTICAL_PADDING = 1.0;
    /** How much extra vertical space, in units of Qwirkle piece grid squares,
     *  do we allow when in horizontal mode, for labels? */
    public static final double HORIZONTAL_PADDING = 0.4;
    /** The size for the labels. */
    public static final double AUTO_SIZE_FRACTION = 0.23;

    private QwirklePlayer player;

    private SwingPlayerHand handPanel;
    private AutoSizeLabel nameLabel, scoreLabel, scoreSeparatorLabel;
    private SwingTurnHighlightLabel bestMoveLabel;
    private Boolean vertical = null;
    private TurnCompleted bestMove = null;
    private boolean myTurn;
    private int handSize;

    private Container labels = null;
    private Set<AutoSizeLabel> autoSizeLabels = new HashSet<>();

    public SwingPlayer(final QwirkleUIController control, final QwirklePlayer player) {
        this.player = player;
        this.handPanel = new SwingPlayerHand(control.getInteraction().getHandTracker(player));

        setLayout(new GridBagLayout());
        nameLabel = new AutoSizeLabel(this, " -- ", AUTO_SIZE_FRACTION); // text is set later
        autoSizeLabels.add(nameLabel);
        scoreLabel = new AutoSizeLabel(this, "0", AUTO_SIZE_FRACTION);
        autoSizeLabels.add(scoreLabel);
        scoreSeparatorLabel = new AutoSizeLabel(this, ": ", AUTO_SIZE_FRACTION);
        autoSizeLabels.add(scoreSeparatorLabel);
        bestMoveLabel = new SwingTurnHighlightLabel(control.getEventBus(), this, AUTO_SIZE_FRACTION,
                new Callable<TurnCompleted>() { @Override public TurnCompleted call() { return bestMove; } });
        bestMoveLabel.setOpaque(false);
        autoSizeLabels.add(bestMoveLabel);
        setVertical(true);
        handSize = control.getGame().getSettings().getHandSize();

        new SelfDisposingEventSubscriber(control.getEventBus(), new SwingPlatformAttacher(this)) {
            @Subscribe
            public void turnCompleted(TurnCompleted event) {
                AnnotatedGame annotated = event.getStatus().getAnnotated();
                scoreLabel.setText("" + annotated.getScore(player));
                setBestMove(annotated.getBestTurn(player));
            }

            @Subscribe
            public void turnStart(TurnStarting event) {
                setMyTurn(event.getCurPlayer() == player);
            }

            @Subscribe
            public void gameStart(GameStarted started) {
                clear();
                handSize = started.getSettings().getHandSize();
            }
        };
    }

    /** The width to height ratio of this panel. */
    public static double getAspectRatio(boolean vertical, int handSize) {
        if (vertical)
            return 1. / (VERTICAL_PADDING + handSize);
        else
            return ((double) handSize) / (1. + HORIZONTAL_PADDING);
    }

    @Override
    public double getAspectRatio() {
        return getAspectRatio(vertical, handSize);
    }

    private void setBestMove(TurnCompleted best) {
        this.bestMove = best;
        if (best == null) {
            bestMoveLabel.setText(" ");
            bestMoveLabel.setToolTipText("");
        }
        else {
            bestMoveLabel.setText("Best: " + best.getScore() + " ");
            bestMoveLabel.setToolTipText("Best turn in this game: " + best.getSummary());
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

    private void setMyTurn(boolean myTurn) {
        if (myTurn != this.myTurn) {
            this.myTurn = myTurn;
            // TODO make highlighting the current player a little nicer
            if (myTurn) {
                handPanel.makeDraggable(player);
                setBorder(BorderFactory.createLineBorder(new Color(UIConstants.FG.getColorInt())));
                setBackground(new Color(UIConstants.BG_HL.getColorInt()));
            }
            else {
                handPanel.makeUndraggable();
                setBorder(null);
                setBackground(new Color(UIConstants.BG.getColorInt()));
            }
            repaint();
        }
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
            for (AutoSizeLabel asl : autoSizeLabels)
                asl.setMetric(FontAutosizer.Metric.WIDTH);

            // name at top
            constraints.weighty = 0;
            nameLabel.setText(player.getName());
            nameLabel.setToolTipText(player.getName());
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(nameLabel, constraints);
            constraints.gridy++;

            // then score
            constraints.weighty = 0;
            scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(scoreLabel, constraints);
            // add score separator so that it is included in child processing (set background etc)
            constraints.gridx++;
            add(scoreSeparatorLabel, constraints);
            scoreSeparatorLabel.setVisible(false);
            constraints.gridx = 0;
            constraints.gridy++;

            // then hand
            constraints.weighty = 1;
            add(handPanel, constraints);
            constraints.gridy++;

            // then best play at bottom
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
            nameLabel.setText(" " + player.getName());
            labels.add(nameLabel);
            labels.add(scoreSeparatorLabel);
            scoreSeparatorLabel.setVisible(true); // was hidden in vertical
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
