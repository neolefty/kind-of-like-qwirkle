package qwirkle.test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TestMain {
    public static void main(String[] args) {
        checkAssert();

        TestBoard.main(args);
        TestScripted.main(args);
        TestPerformance.main(args);

        System.out.println();
        System.out.println("All tests pass.");
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
