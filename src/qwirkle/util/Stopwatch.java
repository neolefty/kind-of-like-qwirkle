package qwirkle.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stopwatch {
    private final long start = System.currentTimeMillis();
    private long latest = start;
    private boolean printLive = false;
    private boolean firstTime = true;

    // raw timestamps, in sequential order of mark() calls
    private List<Long> markTimes = new ArrayList<>();
    // ditto for labels -- duplicates are preserved
    private List<String> markLabels = new ArrayList<>();

    // duplicate labels are overwritten -- not preserved
    private Map<String, Long> markMap = new HashMap<>();

    public Stopwatch() {}
    public Stopwatch(boolean printLive) { this.printLive = printLive; }

    /** Mark that something has completed. */
    public synchronized void mark(String label) {
        long prev = latest;
        latest = System.currentTimeMillis();
        markTimes.add(latest);
        markLabels.add(label);
        long elapsed = latest - prev;
        markMap.put(label, elapsed);
        if (printLive) {
            System.out.print((firstTime ? "" : ", ") + label + ": " + elapsed);
            firstTime = false;
        }
    }

    public synchronized void mark (long i) {
        mark("" + i);
    }

    /** The time elapsed since the Stopwatch started. */
    public long getElapsed() { // no need to synchronize
        return System.currentTimeMillis() - start;
    }

    /** The time elapsed for a particular marked task. */
    public synchronized long getElapsed(String label) {
        if (!markMap.containsKey(label))
            throw new IllegalArgumentException("unknown label: \"" + label + "\"");
        return markMap.get(label);
    }

    /** Should we <tt>System.out.print(".")</tt> every time {@link #mark} is called? Default false. */
    public void setPrintLive(boolean printLive) { this.printLive = printLive; }

    /** Maybe not what you expect: If any marks have been made, then the time to the last one.
     *  Otherwise -- if no marks have been made -- the time until now. */
    public synchronized long getTotal() {
        if (markTimes.isEmpty())
            return getElapsed();
        else
            return markTimes.get(markTimes.size() - 1) - start;
    }

    @Override
    public synchronized String toString() {
        StringBuilder result = new StringBuilder();
        long prev = start;
        for (int i = 0; i < markLabels.size(); ++i) {
            result.append(markLabels.get(i)).append(": ")
                    .append(markTimes.get(i) - prev)
                    .append(i == markTimes.size() - 1 ? "" : ", ");
            prev = markTimes.get(i);
        }
        result.append(markTimes.isEmpty() ? "" : "; total ")
                .append(getTotal()).append(" ms");
        return result.toString();
    }

    public double getAverage() {
        return ((double) (getTotal())) / markTimes.size();
    }
}
