package org.codegenerator;

import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class SimpleTest {
    @Test
    public void simpleBaseTest() {
        ClassFieldExtractor classFieldExtractor = new ClassFieldExtractor();
        ClzBase clzBase = new ClzBase();
        Node nd = classFieldExtractor.extract(clzBase);
        print(nd);
    }

    @Test
    public void simpleTest() {
        ClassFieldExtractor classFieldExtractor = new ClassFieldExtractor();
        Clz clzBase = new Clz();
        Node nd = classFieldExtractor.extract(clzBase);
        print(nd);
    }

    @Test
    public void referenceTest() {
        NodeIN node1 = new NodeIN();
        NodeIN node2 = new NodeIN();

        node1.next = node2;
        node2.next = node1;

        ClassFieldExtractor classFieldExtractor = new ClassFieldExtractor();
        var ext = classFieldExtractor.extract(node1);

        print(ext);
    }

    @Test
    public void referenceToYourselfTest() {
        NodeIN node1 = new NodeIN();

        node1.next = node1;

        ClassFieldExtractor classFieldExtractor = new ClassFieldExtractor();
        var ext = classFieldExtractor.extract(node1);

        print(ext);
    }

    public void print(Node node) {
        print(node, 1);
    }

    public void print(@NotNull Node node, int indent) {
        if (node.isLeaf()) {
            System.out.printf("%s%s -> %s\n", " ".repeat(indent), node.getRoot(), node.getValue());
        } else {
            System.out.printf("%s%s -> %s\n", " ".repeat(indent), node.getRoot(), node.getValue());
            for (var e : node.entrySet()) {
                print(e.getValue(), indent + 1);
            }
        }
    }
}
