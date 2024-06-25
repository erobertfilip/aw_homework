package homework;

import homework.tools.Tools;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class TestRunners {

    Tools t;

    public TestRunners() throws IOException {
        t = new Tools();
    }

    @Before
    public void resetIterator() {
        t.resetIterator();
    }

    @Test
    public void test1() {
        t.getTopDldShowPerCity("San Francisco");
    }

    @Test
    public void test2() {
        t.getMostUsedDeviceType();
    }

    @Test
    public void test3() {
        t.getPreRollOpportunities("preroll");
    }
}

