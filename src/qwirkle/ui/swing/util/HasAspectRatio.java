package qwirkle.ui.swing.util;

/** Something that likes to preserve a certain aspect ratio. */
public interface HasAspectRatio {
    double getAspectRatio();
    boolean isVertical();
    void setVertical(boolean vertical);
}
