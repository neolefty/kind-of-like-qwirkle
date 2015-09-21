package qwirkle.test;

import com.google.common.eventbus.EventBus;
import qwirkle.control.GameModel;
import qwirkle.control.impl.SingleThreadedStrict;
import qwirkle.game.AsyncPlayer;
import qwirkle.game.QwirklePlayer;
import qwirkle.game.QwirkleSettings;
import qwirkle.game.impl.AsyncPlayerWrapper;

import java.util.Collection;

public class PlayTester {
    private final GameModel game;

    public PlayTester(QwirkleSettings settings) {
        this.game = new GameModel(new EventBus(), settings, new SingleThreadedStrict());
    }

    public PlayTester(Collection<QwirklePlayer> players) {
        this(new QwirkleSettings(AsyncPlayerWrapper.wrap(players)));
    }

    /** Play once and return the winner. */
    public AsyncPlayer play() {
        return play(3);
    }

    public GameModel getGame() {
        return game;
    }

    /** Play once and return the winner.
     *  @param nDecks the number of sets of tiles to use (default 3). */
    public AsyncPlayer play(int nDecks) {
        QwirkleSettings settings = game.getSettings();
        settings = new QwirkleSettings(nDecks, settings.getShapes(),
                settings.getColors(), settings.getPlayers());
        play(settings);
        return game.getStatus().getLeader();
    }

    private void play(QwirkleSettings settings) {
        game.start(settings);
        while (!game.isFinished()) {
            game.step();
//            QwirklePlayer cur = game.getCurrentPlayer().get();
//            List<QwirklePlacement> play = game.step();
//            QwirkleBoard board = game.getBoard().get();
//            System.out.println("Player \"" + cur.getName() + "\" plays: " + play
//                    + " for " + board.getLastScore() + " points:");
//            System.out.println(board);
        }
    }
}
