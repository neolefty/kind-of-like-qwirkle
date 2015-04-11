package qwirkle.ui.board;

import qwirkle.control.GameManager;
import qwirkle.control.GameStatus;
import qwirkle.game.QwirklePlayer;
import qwirkle.game.QwirkleTurn;
import qwirkle.ui.util.AutoSizeLabel;

import javax.swing.*;

/** Show the current scores. */
public class QwirkleScorePanel extends JPanel {
    // TODO auto-fit?
    // TODO update when players change
    public QwirkleScorePanel(GameManager game, double fontSize) {
        // score labels - update when score changes
        boolean first = true;
        for (final QwirklePlayer player : game.getPlayers()) {
            final JLabel score = new AutoSizeLabel(this, "", fontSize);
//            setColors(score);
            game.getStatus().listen(new GameStatus.TurnListener() {
                @Override
                public void turn(final QwirkleTurn turn) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            String summary = player.getName();
                            if (turn != null && turn.getStatus() != null)
                                summary += ": " + turn.getStatus().getScore(player);
                            score.setText(summary);
                        }
                    });
                }
            });
            add(score);
            if (!first)
                add(new AutoSizeLabel(this, "  ", fontSize));
            first = false;
        }
    }
}
