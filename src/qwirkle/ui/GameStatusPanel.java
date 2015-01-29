package qwirkle.ui;

import com.google.common.eventbus.Subscribe;
import qwirkle.control.GameManager;
import qwirkle.control.GameOver;
import qwirkle.control.GameStatus;
import qwirkle.game.QwirkleTurn;

import javax.swing.*;

/** Show the status messages from a game. */
public class GameStatusPanel extends Box {
    // TODO show scrolling messages "joe plays 3 pieces for 6 points ... bob wins with 192 points ... repeat"
    // Show the status of the current turn and the overall game
    JLabel turnLabel, gameLabel;
    public GameStatusPanel(GameManager mgr) {
        super(BoxLayout.X_AXIS);
        turnLabel = new JLabel();
        gameLabel = new JLabel();
        add(turnLabel, Box.LEFT_ALIGNMENT);
        add(Box.createGlue()); // fill space between labels
        add(gameLabel, Box.RIGHT_ALIGNMENT);
        mgr.getEventBus().register(new Object() {
            @Subscribe
            public void gameOver(GameOver gameOver) {
                finished(gameOver.getStatus());
            }
//            @Subscribe public void gameStarted(GameStarted started) {
//                gameLabel.setText("");
//            }
            @Subscribe public void status(GameStatus status) {
                if (status.isFinished())
                    finished(status);
                else if (status.getAnnotatedGame() != null
                        && status.getAnnotatedGame().getBestTurn() != null)
                    gameLabel.setText("Best so far: "
                            + status.getAnnotatedGame().getBestTurn().getSummary());
            }
            @Subscribe public void turn(QwirkleTurn turn) {
                turnLabel.setText(turn.getSummary());
            }
        });
    }

    private void finished(GameStatus status) {
        gameLabel.setText("Game Over: " + status.getFinishedMessage());
    }
}
