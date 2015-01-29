package qwirkle.test;

import qwirkle.game.QwirklePlacement;
import qwirkle.game.QwirkleSettings;
import qwirkle.game.impl.QwirkleBoardImpl;

/** Add a convenience method for testing. */
public class QwirkleTestBoard extends QwirkleBoardImpl {
    // A test board with default setup
    public QwirkleTestBoard() { this(new QwirkleSettings()); }
    public QwirkleTestBoard(QwirkleSettings settings) { super(settings); }
    public QwirkleTestBoard(QwirkleBoardImpl play) { super(play); }
    public QwirkleTestBoard play(String abbrev, int x, int y) {
        return new QwirkleTestBoard(play(new QwirklePlacement(abbrev, x, y)));
    }
}
