package qwirkle.test;

import qwirkle.control.GameManager;
import qwirkle.game.QwirkleSettings;
import qwirkle.game.QwirklePlayer;

import java.util.Collection;

public class PlayTester {
    private final GameManager game;

    public PlayTester(QwirkleSettings settings) {
        this.game = new GameManager(settings);
    }

    public PlayTester(Collection<QwirklePlayer> players) {
        this(new QwirkleSettings(players));
    }

    /** Play once and return the winner. */
    public QwirklePlayer play() {
        return play(3);
    }

    public GameManager getGame() {
        return game;
    }

    /** Play once and return the winner.
     *  @param nDecks the number of sets of tiles to use (default 3). */
    public QwirklePlayer play(int nDecks) {
        QwirkleSettings settings = game.getSettings();
        settings = new QwirkleSettings(nDecks, settings.getShapes(), settings.getColors(), settings.getPlayers());
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
//            System.out.println("Player \"" + cur.getName() + "\" plays: " + play + " for " + board.getLastScore() + " points:");
//            System.out.println(board);
        }
    }
}
