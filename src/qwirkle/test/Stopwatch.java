package qwirkle.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stopwatch {
    private final long start = System.currentTimeMillis();
    private long latest = start;

    // raw timestamps, in sequential order of mark() calls
    private List<Long> markTimes = new ArrayList<>();
    // ditto for labels -- duplicates are preserved
    private List<String> markLabels = new ArrayList<>();

    // duplicate labels are overwritten -- not preserved
    private Map<String, Long> markMap = new HashMap<>();

    /** Mark that something has completed. */
    public synchronized void mark(String label) {
        long prev = latest;
        latest = System.currentTimeMillis();
        markTimes.add(latest);
        markLabels.add(label);
        markMap.put(label, latest - prev);
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
}
