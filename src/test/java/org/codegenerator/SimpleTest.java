package org.codegenerator;

import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.junit.jupiter.api.Test;

public class SimpleTest {
    public class ClzBase {
        int x = 10;
    }

    public class Clz extends ClzBase {
        private final int y = 12;
        private final ClzBase base = new ClzBase();

        {
            base.x = 42;
        }
    }

    @Test
    public void simpleTest() {
        ClassFieldExtractor classFieldExtractor = new ClassFieldExtractor();
        Clz clzBase = new Clz();
        Node nd = classFieldExtractor.extract(clzBase);
        System.out.println(nd);
    }
}
