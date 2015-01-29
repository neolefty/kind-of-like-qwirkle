package qwirkle.game;

public class QwirklePlacement implements HasQwirkleLocation, Comparable<QwirklePlacement> {
    private QwirkleLocation location;
    private QwirklePiece piece;

    public QwirklePlacement(String abbrev, int x, int y) {
        this(new QwirklePiece(abbrev), x, y);
    }

    public QwirklePlacement
            (QwirklePiece piece, QwirkleLocation location)
    {
        if (location == null)
            throw new NullPointerException("location is null");
        if (piece == null)
            throw new NullPointerException("piece is null");
        this.location = location;
        this.piece = piece;
    }

    public QwirklePlacement(QwirklePiece piece, int x, int y) {
        this(piece, new QwirkleLocation(x, y));
    }

    public QwirklePiece getPiece() { return piece; }
    @Override
    public QwirkleLocation getQwirkleLocation() { return location; }
    public QwirkleLocation getLocation() { return location; }

    @Override
    public int compareTo(QwirklePlacement that) {
        return (that == null) ? 1
                : (that == this ? 0
                : (location.compareTo(that.location)));
    }

    public int getX() { return location.getX(); }
    public int getY() { return location.getY(); }
    public QwirkleShape getShape() { return piece.getShape(); }
    public QwirkleColor getColor() { return piece.getColor(); }

    @Override
    public String toString() {
        return piece + " at " + location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QwirklePlacement that = (QwirklePlacement) o;
        return location.equals(that.location) && piece.equals(that.piece);
    }

    @Override
    public int hashCode() {
        int result = location.hashCode();
        result = 31 * result + piece.hashCode();
        return result;
    }
}
