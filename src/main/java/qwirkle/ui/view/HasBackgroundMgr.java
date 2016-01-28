package qwirkle.ui.view;

/** A UI element that has a {@link BackgroundManager}. */
public interface HasBackgroundMgr extends HasBackground {
    BackgroundManager getBackgroundManager();
}
