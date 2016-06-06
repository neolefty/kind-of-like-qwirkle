package qwirkle.test;

import qwirkle.util.Stopwatch;

/** Tests that run faster. */
public class TestMainBrief {
    public static void main(String[] args) throws InterruptedException {
        Stopwatch w = new Stopwatch(false);
        main(args, w);
        System.out.println("Brief tests pass: " + w);
    }

    public static void main(String[] args, Stopwatch w) throws InterruptedException {
        TestMain.checkAssert();
        TestBoard.main(args); w.mark("board");
        TestScripted.main(args); w.mark("scripted");
        TestRainbow.main(args); w.mark("rainbow");
    }
}
