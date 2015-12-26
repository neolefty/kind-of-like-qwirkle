package qwirkle.test.visual;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import qwirkle.ui.UIConstants;
import qwirkle.ui.swing.util.SwingSetup;
import qwirkle.ui.swing.util.SwingUserActivityTimeout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Test {@link SwingUserActivityTimeout} */
public class TestUserActivityTimeout {
    public static void main(String[] args) {
        final int seconds = 4;
        JFrame frame = new JFrame("Waiting for timeout");
        SwingSetup.addWindowSizer(frame, SwingUserActivityTimeout.class);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Box box = new Box(BoxLayout.Y_AXIS);
        frame.setContentPane(box);
        EventBus bus = new EventBus();
        JPanel watched = new JPanel();
        final SwingUserActivityTimeout timeout = new SwingUserActivityTimeout(watched, bus, seconds * 1000, 100);

        box.add(watched);
        final JLabel label = new JLabel("Waiting for " + seconds + " seconds of inactivity");
        watched.add(label);

        bus.register(new Object() {
            @Subscribe
            public void timeout(SwingUserActivityTimeout.TimeoutEvent event) {
                label.setText("Inactive for " + event.getElapsedMillis() + "ms");
            }
            @Subscribe public void resume(SwingUserActivityTimeout.ResumeEvent event) {
                label.setText("Resumed. Waiting for " + seconds + ".");
            }
        });

        JPanel controls = new JPanel();
        box.add(controls);
        controls.setBackground(new Color(UIConstants.BG.getColorInt()));

        JButton activateButton = new JButton("Activate");
        controls.add(activateButton);
        activateButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { timeout.activityDetected(); }
        });

        JButton timeoutButton = new JButton("Timeout");
        controls.add(timeoutButton);
        timeoutButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { timeout.timeout(); }
        });

        JButton quitButton = new JButton("Quit");
        controls.add(quitButton);
        quitButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (timeout.quit())
                    label.setText("Not watching");
            }
        });

        JButton resumeButton = new JButton("Resume");
        controls.add(resumeButton);
        resumeButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (timeout.resume())
                    label.setText("Resumed watching; waiting for " + seconds);
            }
        });

        frame.setVisible(true);
    }
}
