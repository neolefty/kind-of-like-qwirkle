package qwirkle.ui.view.colors;

import qwirkle.game.base.QwirkleColor;
import qwirkle.game.control.players.Rainbow;

import java.util.*;

/** Map the game's colors into a different palette for the local UI. Useful for a
 *  network game, for example, where different players want different palettes. */
public class LocalPalette {
    private Rainbow rainbow;
    private List<Integer> localColors;
    private Map<QwirkleColor, Integer> colorMap;

    public LocalPalette(Collection<Integer> localColors, Collection<QwirkleColor> qwirkleColors) {
        if (localColors.size() < qwirkleColors.size())
            throw new IllegalArgumentException
                    ("not enough colors (" + localColors.size() + " vs " + qwirkleColors.size() + ")");
        rainbow = new Rainbow(qwirkleColors);
        this.localColors = new ArrayList<>(localColors);
        colorMap = assignColors(localColors, qwirkleColors);
    }

    public int getColor(QwirkleColor qc) {
        return colorMap.get(qc);
    }

    private Map<QwirkleColor, Integer> assignColors(Collection<Integer> localColors, Collection<QwirkleColor> qs) {
        Set<Integer> remaining = new HashSet<>(localColors);
        Map<QwirkleColor, Integer> result = new HashMap<>();
        for (QwirkleColor q : qs) {
            Integer next = closest(q, remaining);
            remaining.remove(next);
            result.put(q, next);
        }
        return result;
    }

    private Integer closest(QwirkleColor qwirkleColor, Set<Integer> colors) {
        int min = Integer.MAX_VALUE;
        Integer result = null;
        for (Integer c : colors) {
            int d = Rainbow.colorDistance(qwirkleColor.getColorInt(), c);
            if (d < min) {
                min = d;
                result = c;
            }
        }
        return result;
    }
}
