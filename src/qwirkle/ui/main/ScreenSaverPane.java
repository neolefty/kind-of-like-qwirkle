package qwirkle.ui.main;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.ui.swing.UserActivityTimeout;

import javax.swing.*;
import java.awt.*;

/** A panel that switches between a screen saver and main UI */
public class ScreenSaverPane extends JPanel {
    private static final String KEY_MAIN = "main", KEY_SS = "screensaver";
    public static final long FADE_MILLIS = 250;

    private CardLayout layout = new CardLayout();
    private UserActivityTimeout timeout;
    private long fadeMillis = FADE_MILLIS;

    /** Fader is optional. */
    public ScreenSaverPane
        (Component main, final Component screensaver, final Fader fader, long sleepMillis)
    {
        setLayout(layout);

        add(main);
        add(screensaver);
        layout.addLayoutComponent(main, KEY_MAIN);
        layout.addLayoutComponent(screensaver, KEY_SS);

        EventBus bus = new EventBus();
        timeout = new UserActivityTimeout(main, bus, sleepMillis, 150);
        timeout.addWatched(screensaver);
        timeout.setDebugging(false);

        // watch controls too because they intercept actions
        watchControls(main);
        watchControls(screensaver);

        bus.register(new Object() {
            @Subscribe public void timeout(UserActivityTimeout.TimeoutEvent event) {
                layout.show(ScreenSaverPane.this, KEY_SS);
            }

            @Subscribe public void resume(UserActivityTimeout.ResumeEvent event) {
                if (fader != null) {
                    fader.fade(fadeMillis, new Runnable() {
                        @Override public void run() { showMain(); }
                    });
                }
                else
                    showMain();
            }
        });
    }

    public void showMain() {
        layout.show(this, KEY_MAIN);
    }

    /** How long to fade the screensaver away. */
    public void setFadeMillis(long fadeMillis) { this.fadeMillis = fadeMillis; }

    /** Recursively look for controls (descendents of AbstractButton) and monitor them
     *  for keyboard and mouse events because they tend to intercept mouse events,
     *  so our top-level components would otherwise miss them. */
    private void watchControls(Component c) {
        if (c instanceof AbstractButton) {
            timeout.addWatched(c);
        }
        if (c instanceof Container) {
            Container parent = (Container) c;
            for (Component child : parent.getComponents())
                watchControls(child);
        }
    }

    public interface Fader {
        void fade(long millis, Runnable callback);
    }
}
