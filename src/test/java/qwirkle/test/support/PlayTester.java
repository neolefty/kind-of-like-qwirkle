package qwirkle.test.support;

import com.google.common.eventbus.EventBus;
import qwirkle.game.base.QwirkleAI;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.base.QwirkleSettings;
import qwirkle.game.control.GameController;
import qwirkle.game.control.impl.SingleThreadedStrict;

import java.util.Collection;

public class PlayTester {
    private final GameController game;

    public PlayTester(QwirkleSettings settings) {
        this.game = new GameController(new EventBus(), settings, new SingleThreadedStrict());
    }

    public PlayTester(Collection<QwirkleAI> players) {
        this(new QwirkleSettings(QwirklePlayer.wrap(players)));
    }

    /** Play once and return the winner. */
    public QwirklePlayer play() {
        return play(3);
    }

    public GameController getGame() {
        return game;
    }

    /** Play once and return the winner.
     *  @param nDecks the number of sets of tiles to use (default 3). */
    public QwirklePlayer play(int nDecks) {
        QwirkleSettings settings = game.getSettings();
        settings = new QwirkleSettings(nDecks, settings.getShapes(),
                settings.getColors(), settings.getPlayers());
        play(settings);
        return game.getAnnotated().getLeader();
    }

    private void play(QwirkleSettings settings) {
        game.start(settings);
        while (!game.isFinished()) {
            game.stepAI();
//            QwirklePlayer cur = game.getCurrentPlayer().get();
//            List<QwirklePlacement> play = game.stepAI();
//            QwirkleBoard board = game.getBoard().get();
//            System.out.println("Player \"" + cur.getName() + "\" plays: " + play
//                    + " for " + board.getLastScore() + " points:");
//            System.out.println(board);
        }
    }
}
