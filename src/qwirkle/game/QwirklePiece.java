package qwirkle.game;

/** A piece you can play in the game Qwirkle.
 *  Immutable -- you can't change the color or shape. */
public class QwirklePiece {
    private QwirkleColor color;
    private QwirkleShape shape;

    public QwirklePiece(String abbrev) {
        this(QwirkleColor.pick(abbrev.substring(0, 1)),
                QwirkleShape.pick(abbrev.substring(1, 2)));
    }

    public QwirklePiece
            (QwirkleColor color, QwirkleShape shape)
    {
        if (color == null)
            throw new NullPointerException("color is null");
        if (shape == null)
            throw new NullPointerException("shape is null");
        this.color = color;
        this.shape = shape;
    }

    /** An abbreviation of <tt>pieces</tt>, for example "[ p8 bs bc bd gs gc ]". */
    public static String abbrev(Iterable<QwirklePiece> pieces) {
        StringBuilder result = new StringBuilder().append("[ ");
        for (QwirklePiece p : pieces)
            result.append(p.getAbbrev()).append(" ");
        result.append("]");
        return result.toString();
    }

    public QwirkleColor getColor() { return color; }
    public QwirkleShape getShape() { return shape; }

    @Override
    public String toString() { return color + " " + shape; }

    public String getAbbrev() {
        return color.getAbbrev() + shape.getAbbrev();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        else if (o == null || getClass() != o.getClass())
            return false;
        else {
            QwirklePiece that = (QwirklePiece) o;
            return (color == that.color && shape == that.shape);
        }
    }

    @Override
    public int hashCode() {
        int result = color.hashCode();
        result = 31 * result + shape.hashCode();
        return result;
    }
}
