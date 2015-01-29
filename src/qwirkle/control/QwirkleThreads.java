package qwirkle.control;

/** Automatically run a game, pause it, etc. Posts itself to the EventBus whenever
 *  thread status changes. */
public class QwirkleThreads extends WriterImpl<QwirkleThreads.Filament> {
    private static final long DEFAULT_STEP_MILLIS = 1000;
    private static final long TICK_MILLIS = 50;

    private GameManager game;
    private long stepMillis = DEFAULT_STEP_MILLIS;

    public QwirkleThreads(GameManager game) {
        init(this, game.getEventBus());
        this.game = game;
    }

    public boolean isRunning() { return get() != null; }

    @Override
    public void set(Filament filament) {
        throw new IllegalStateException("Only internal.");
    }

    public synchronized void go() {
        if (get() == null) {
            Filament f = new Filament();
            super.set(f);
            f.start();
        }
    }

    public synchronized void stop() {
        if (get() != null) {
            get().done();
        }
    }

    public long getStepMillis() { return stepMillis; }
    public void setStepMillis(long stepMillis) { this.stepMillis = stepMillis; }

    public class Filament extends Thread {
        private boolean going = false;

        @Override
        public void run() {
            try {
                going = true;
                while (going) {
                    long start = System.currentTimeMillis();
                    game.step();
                    if (!game.isFinished())
                        sleepFrom(start);
                }
            } catch (InterruptedException ignored) {
            } catch (Exception e) {
                e.printStackTrace();
            }
            done();
            QwirkleThreads.super.set(null);
        }

        private void sleepFrom(long start) throws InterruptedException {
            // if stepMillis is changed, handle it right away
            // wake up to check for a change every TICK_MILLIS milliseconds
            long end = start + stepMillis;
            long remain = end - System.currentTimeMillis();
            while (remain > 0 && going) {
                sleep(Math.min(remain, TICK_MILLIS));
                remain = end - System.currentTimeMillis();
            }
        }

        /** This filament is terminating or should terminate. */
        public void done() {
            going = false;
        }
    }
}
