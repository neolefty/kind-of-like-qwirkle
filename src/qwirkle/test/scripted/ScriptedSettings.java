package qwirkle.test.scripted;

import com.google.common.base.Splitter;
import qwirkle.game.base.QwirklePiece;
import qwirkle.game.base.QwirklePlayer;
import qwirkle.game.base.QwirkleSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** GameSettings for a scripted game. */
public class ScriptedSettings extends QwirkleSettings {
    private String deckPrefix;

    /** When you use this, make sure that the first player actually deserves to go first
     *  (has the best first play) by the rules of the game. */
    public ScriptedSettings(Collection<QwirklePlayer> players) {
        super(players, 1);
    }

    /** Pieces to add to the beginning of the deck. Comma-separated,
     *  for example "bc,yc" means add a blue circle and a yellow circle
     *  to the beginning of the deck -- they'll be dealt
     *  once the scripted moves have been exhausted. */
    public void setDeckPrefix(String deckPrefix) {
        this.deckPrefix = deckPrefix;
    }

    /** Add the scripted pieces to the start of the deck. */
    @Override
    public List<QwirklePiece> generate() {
        List<QwirklePiece> result = new ArrayList<>();
        if (deckPrefix != null)
            for (String s : Splitter.on(',').split(deckPrefix))
                if (s != null && !s.isEmpty())
                    result.add(new QwirklePiece(s));
        result.addAll(super.generate());
        return result;
    }

    /** Same thing, but with a custom set of shapes and colors. */
    public ScriptedSettings(Collection<QwirklePlayer> players, String shapes, String colors) {
        super(1, shapes, colors, players);
    }
}
