package qwirkle.ui.view;

/** Something that likes to preserve a certain aspect ratio. */
public interface HasAspectRatio {
    /** ideal width to height */
    double getAspectRatio();
    boolean isVertical();
    void setVertical(boolean vertical);
}
