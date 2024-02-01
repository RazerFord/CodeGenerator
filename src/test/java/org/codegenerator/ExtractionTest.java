package org.codegenerator;

import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.codegenerator.resourcesfieldextraction.Clz;
import org.codegenerator.resourcesfieldextraction.ClzBase;
import org.codegenerator.resourcesfieldextraction.ListNode;
import org.codegenerator.resourcesfieldextraction.SameValues;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;

class ExtractionTest {
    @Test
    void simpleBaseTest() {
        ClzBase clzBase = new ClzBase();
        Node extracted = ClassFieldExtractor.extract(clzBase);
        print(extracted);
    }

    @Test
    void sameValuesTest() {
        SameValues sameValues = new SameValues();
        Node extracted = ClassFieldExtractor.extract(sameValues);
        print(extracted);
    }

    @Test
    void nullTest() {
        ListNode node = new ListNode();
        Node extracted = ClassFieldExtractor.extract(node);
        print(extracted);
    }

    @Test
    void simpleInheritanceTest() {
        Clz clz = new Clz();
        Node extracted = ClassFieldExtractor.extract(clz);
        print(extracted);
    }

    @Test
    void referenceTest() {
        ListNode node1 = new ListNode();
        ListNode node2 = new ListNode();

        node1.next = node2;
        node2.next = node1;

        Node extracted = ClassFieldExtractor.extract(node1);

        print(extracted);
    }

    @Test
    void referenceToYourselfTest() {
        ListNode node1 = new ListNode();

        node1.next = node1;

        Node extracted = ClassFieldExtractor.extract(node1);

        print(extracted);
    }

    @Test
    void arrayTest() {
        String[] strings = new String[]{"it", "is", "array", "of", "string"};

        Node extracted = ClassFieldExtractor.extract(strings);

        print(extracted);
    }

    @Test
    void listTest() {
        List<String> strings = new ArrayList<>();
        strings.add("it");
        strings.add("is");
        strings.add("array");
        strings.add("of");
        strings.add("string");

        Node extracted = ClassFieldExtractor.extract(strings);

        print(extracted);
    }

    @Test
    void treeSetTest() {
        Set<String> strings = new TreeSet<>();
        strings.add("it");
        strings.add("is");
        strings.add("array");
        strings.add("of");
        strings.add("string");

        Node extracted = ClassFieldExtractor.extract(strings);

        print(extracted);
    }

    @Test
    void hashSetTest() {
        Set<String> strings = new HashSet<>();
        strings.add("it");
        strings.add("is");
        strings.add("array");
        strings.add("of");
        strings.add("string");

        Node extracted = ClassFieldExtractor.extract(strings);

        print(extracted);
    }

    @Test
    void primitivesTest() {
        int i = 42;

        Node extracted = ClassFieldExtractor.extract(i);

        print(extracted);
    }

    void print(@NotNull Node node) {
        Set<Object> visited = new HashSet<>();
        print(node, 0, visited);
    }

    void print(@NotNull Node node, int indent, @NotNull Set<Object> visited) {
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
        } else if (node.nodeType() == Node.NodeType.ARRAY) {
            for (Map.Entry<Object, Node> e : node.entrySet()) {
                Integer key = (Integer) e.getKey();
                String prefix = String.format("%s index:[%s] -> ", String.join("", Collections.nCopies(indent + 1, " ")), key);
                System.out.printf(prefix);
                print(e.getValue(), prefix.length() + 1, visited);
            }
        }
    }
}
