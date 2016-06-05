package qwirkle.test;

import qwirkle.util.Stopwatch;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TestMain {
    public static void main(String[] args) throws InterruptedException {
        checkAssert();
        Stopwatch w = new Stopwatch(false);

        TestBoard.main(args); w.mark("board");
        TestScripted.main(args); w.mark("scripted");
        TestThreads.main(args); w.mark("threads");
        TestRainbow.main(args); w.mark("rainbow");
        TestPerformance.main(args); w.mark("performance");

        System.out.println();
        System.out.println("All tests pass: " + w);
    }

    public static <T> void checkContentsMatch(Collection<T> a, Collection<T> b) {
        Set<T> sa = new HashSet<>(a);
        Set<T> sb = new HashSet<>(b);
        assert sa.equals(sb);
        assert a.size() == b.size();
    }

    public static void checkAssert() {
        try {
            assert false;
            throw new IllegalStateException("Assertions not enabled. Add VM option -ea");
        } catch(AssertionError ignored) {}
    }
}
