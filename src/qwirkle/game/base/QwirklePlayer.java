package qwirkle.game.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Information about a Qwirkle player.
 *  Name, humanity, etc */
public class QwirklePlayer {
    private String name;
    private boolean human;
    private QwirkleAI ai;

    /** Create an AI player. */
    public QwirklePlayer(QwirkleAI ai) {
        this.name = ai.getName();
        this.human = false;
        this.ai = ai;
    }

    /** Create a human player. */
    public QwirklePlayer(String name) {
        this.name = name;
        this.human = true;
    }

    public String getName() { return name; }

    public boolean isHuman() { return human; }

    public QwirkleAI getAi() { return ai; }

    public static List<QwirklePlayer> wrap(Collection<QwirkleAI> ais) {
        List<QwirklePlayer> result = new ArrayList<>();
        for (QwirkleAI ai : ais)
            result.add(new QwirklePlayer(ai));
        return result;
    }

    @Override public String toString() { return getName(); }
}
