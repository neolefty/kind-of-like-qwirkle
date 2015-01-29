package qwirkle.players;

import qwirkle.game.*;

import java.util.*;

public class SmarterPlayer implements QwirklePlayer {
    @Override
    public List<QwirklePlacement> play(QwirkleBoard board, List<QwirklePiece> hand) {
        // 1. organize our pieces by shape and color
        Map<QwirkleShape, Set<QwirklePiece>> byShape = new HashMap<>();
        Map<QwirkleColor, Set<QwirklePiece>> byColor = new HashMap<>();
        for (QwirklePiece piece : hand) {
            QwirkleShape shape = piece.getShape();
            if (!byShape.containsKey(shape))
                byShape.put(shape, new HashSet<QwirklePiece>());
            byShape.get(shape).add(piece);
            QwirkleColor color = piece.getColor();
            if (!byColor.containsKey(color))
                byColor.put(color, new HashSet<QwirklePiece>());
            byColor.get(color).add(piece);
        }
        System.out.println("By color: " + byColor);
        System.out.println("By shape: " + byShape);

        // 2. make a set of all our sets of matching pieces
        Set<Set<QwirklePiece>> allSets = new HashSet<>();
        allSets.addAll(byColor.values());
        allSets.addAll(byShape.values());
        System.out.println("All sets: " + allSets);

        // 4. find largest set of matching pieces
        Set<QwirklePiece> largest = new HashSet<>();
        for (Set<QwirklePiece> set : allSets)
            if (largest.size() < set.size())
                largest = set;
        System.out.println("Largest set: " + largest);

        // 5. see if it fits on the board


        throw new UnsupportedOperationException("What next?");
    }

    private List<QwirklePlacement> getPlaceToPlace
            (QwirkleBoard board, Set<QwirklePiece> setOfPieces)
    {
        List<QwirklePlacement> result = new ArrayList<>();
        // 1. get any piece, and find its legal placements
        List<QwirklePiece> pieces = new ArrayList<>(setOfPieces);
        QwirklePiece firstPiece = pieces.get(0);
        Collection<QwirklePlacement> legal = board.getLegalPlacements(firstPiece);
        // 2. look for a place for the second piece
        if (!legal.isEmpty()) {
            // if our list of pieces only has 1 in it, just return the first possibility
            if (pieces.size() == 1)
                result.add(legal.iterator().next());
            // if we have more than one piece to place,
            // find a good place for the second piece
            else {
                for (QwirklePlacement placement : legal) {
                    // pretend to play that first piece
                    QwirkleBoard newBoard = board.play(placement);
                    List<QwirkleLocation> neighbors = placement.getLocation().getNeighbors();

                }
            }
        }
        throw new UnsupportedOperationException("not finished");
    }

    @Override
    public String getName() {
        return "Sets and Maps";
    }

    @Override
    public Collection<QwirklePiece> discard(QwirkleBoard board, List<QwirklePiece> hand) {
        return hand;
    }
}
