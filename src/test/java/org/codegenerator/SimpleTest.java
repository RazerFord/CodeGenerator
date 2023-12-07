package org.codegenerator;

import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class SimpleTest {
    @Test
    public void simpleBaseTest() {
        ClassFieldExtractor classFieldExtractor = new ClassFieldExtractor();
        ClzBase clzBase = new ClzBase();
        Node nd = classFieldExtractor.extract(clzBase);
        print(nd);
    }

    @Test
    public void sameValuesTest() {
        ClassFieldExtractor classFieldExtractor = new ClassFieldExtractor();
        SameValues sameValues = new SameValues();
        Node nd = classFieldExtractor.extract(sameValues);
        print(nd);
    }

    @Test
    public void nullTest() {
        ClassFieldExtractor classFieldExtractor = new ClassFieldExtractor();
        NodeIN node = new NodeIN();
        Node nd = classFieldExtractor.extract(node);
        print(nd);
    }

    @Test
    public void simpleInheritanceTest() {
        ClassFieldExtractor classFieldExtractor = new ClassFieldExtractor();
        Clz clz = new Clz();
        Node nd = classFieldExtractor.extract(clz);
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

    public void print(@NotNull Node node) {
        Set<Object> visited = new HashSet<>();
        print(node, 0, visited);
    }

    public void print(@NotNull Node node, int indent, @NotNull Set<Object> visited) {
        System.out.printf("%s:%s\n", node.getClassOfValue(), node.getValue());
        if (visited.contains(node)) System.out.printf("%sReference\n", " ".repeat(indent));
        if (!visited.add(node) || node.getClassOfValue() == null) return;
        if (node.isLeaf()) {
            System.out.printf("%s%s\n", " ".repeat(indent), node.getValue());
        } else {
            for (var e : node.entrySet()) {
                String prefix = String.format("%s%s -> ", " ".repeat(indent + 1), e.getKey().getName());
                System.out.printf(prefix);
                print(e.getValue(), prefix.length() + 1, visited);
            }
        }
    }
}
