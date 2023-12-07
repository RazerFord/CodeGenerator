package org.codegenerator;

import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SimpleTest {
    @Test
    public void simpleBaseTest() {
        ClassFieldExtractor classFieldExtractor = new ClassFieldExtractor();
        ClzBase clzBase = new ClzBase();
        Node extracted = classFieldExtractor.extract(clzBase);
        print(extracted);
    }

    @Test
    public void sameValuesTest() {
        ClassFieldExtractor classFieldExtractor = new ClassFieldExtractor();
        SameValues sameValues = new SameValues();
        Node extracted = classFieldExtractor.extract(sameValues);
        print(extracted);
    }

    @Test
    public void nullTest() {
        ClassFieldExtractor classFieldExtractor = new ClassFieldExtractor();
        ListNode node = new ListNode();
        Node extracted = classFieldExtractor.extract(node);
        print(extracted);
    }

    @Test
    public void simpleInheritanceTest() {
        ClassFieldExtractor classFieldExtractor = new ClassFieldExtractor();
        Clz clz = new Clz();
        Node extracted = classFieldExtractor.extract(clz);
        print(extracted);
    }

    @Test
    public void referenceTest() {
        ListNode node1 = new ListNode();
        ListNode node2 = new ListNode();

        node1.next = node2;
        node2.next = node1;

        ClassFieldExtractor classFieldExtractor = new ClassFieldExtractor();
        Node extracted = classFieldExtractor.extract(node1);

        print(extracted);
    }

    @Test
    public void referenceToYourselfTest() {
        ListNode node1 = new ListNode();

        node1.next = node1;

        ClassFieldExtractor classFieldExtractor = new ClassFieldExtractor();
        Node extracted = classFieldExtractor.extract(node1);

        print(extracted);
    }

    public void print(@NotNull Node node) {
        Set<Object> visited = new HashSet<>();
        print(node, 0, visited);
    }

    public void print(@NotNull Node node, int indent, @NotNull Set<Object> visited) {
        System.out.printf("%s:%s\n", node.getClassOfValue(), node.getValue());
        if (visited.contains(node))
            System.out.printf("%sReference\n", String.join("", Collections.nCopies(indent, " ")));
        if (!visited.add(node) || node.getClassOfValue() == null) return;
        if (node.nodeType() == Node.NodeType.LEAF) {
            System.out.printf("%s%s\n", String.join("", Collections.nCopies(indent, " ")), node.getValue());
        } else if (node.nodeType() == Node.NodeType.INNER) {
            for (Map.Entry<Object, Node> e : node.entrySet()) {
                Field key = (Field) e.getKey();
                String prefix = String.format("%s%s -> ", String.join("", Collections.nCopies(indent + 1, " ")), key.getName());
                System.out.printf(prefix);
                print(e.getValue(), prefix.length() + 1, visited);
            }
        }
    }
}
