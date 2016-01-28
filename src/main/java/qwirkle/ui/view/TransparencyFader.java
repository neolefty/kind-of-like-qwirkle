package qwirkle.ui.view;

/** Use HasTransparency to fade something. */
public class TransparencyFader implements Fader {
    private long sleepMillis;
    private HasTransparency victim;

    /** @param sleepMillis how long to sleep in between fading steps? */
    public TransparencyFader(HasTransparency victim, long sleepMillis) {
        this.victim = victim;
        this.sleepMillis = sleepMillis;
    }

    @Override
    public void fade(final long millis, final Runnable callback) {
        final long start = System.currentTimeMillis();
        new Thread() {
            @Override
            public void run() {
                try {
                    long elapsed;
                    do {
                        long startLoop = System.currentTimeMillis();
                        // start at 0 transparency, work up to 1
                        elapsed = startLoop - start;
                        double fraction = ((double) elapsed) / millis;
//                        System.out.println("Fraction = " + fraction);
                        if (fraction > 1)
                            fraction = 1;
                        victim.setTransparency(fraction);
                            Thread.sleep(sleepMillis);
                    } while (elapsed < millis);
                }
                catch (InterruptedException ignored) { }
                finally {
                    resetVictimLater();
                    if (callback != null)
                        callback.run();
                }
            }
        }.start();
    }

    /** Sleep 500 millis and then reset the victim's transparency. */
    private void resetVictimLater() {
        new Thread() {
            @Override
            public void run() {
                try { sleep(500); }
                catch (InterruptedException ignored) { }
                finally { victim.setTransparency(0); }
            }
        }.start();
    }
}
