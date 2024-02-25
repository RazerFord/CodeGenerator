package org.codegenerator;

import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.codegenerator.testclasses.fieldextraction.Clz;
import org.codegenerator.testclasses.fieldextraction.ClzBase;
import org.codegenerator.testclasses.fieldextraction.ListNode;
import org.codegenerator.testclasses.fieldextraction.SameValues;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtractionTest {
    @Test
    void simpleBaseTest() {
        ClzBase clzBase = new ClzBase();

        extractAndPrint(clzBase);
        assertTrue(true);
    }

    @Test
    void sameValuesTest() {
        SameValues sameValues = new SameValues();

        extractAndPrint(sameValues);
        assertTrue(true);
    }

    @Test
    void nullTest() {
        ListNode node = new ListNode();

        extractAndPrint(node);
        assertTrue(true);
    }

    @Test
    void simpleInheritanceTest() {
        Clz clz = new Clz();

        extractAndPrint(clz);
        assertTrue(true);
    }

    @Test
    void referenceTest() {
        ListNode node1 = new ListNode();
        ListNode node2 = new ListNode();

        node1.next = node2;
        node2.next = node1;

        extractAndPrint(node1);
        assertTrue(true);
    }

    @Test
    void referenceToYourselfTest() {
        ListNode node1 = new ListNode();

        node1.next = node1;

        extractAndPrint(node1);
        assertTrue(true);
    }

    @Test
    void arrayTest() {
        String[] strings = new String[]{"it", "is", "array", "of", "string"};

        extractAndPrint(strings);
        assertTrue(true);
    }

    @Test
    void listTest() {
        List<String> strings = new ArrayList<>();
        strings.add("it");
        strings.add("is");
        strings.add("array");
        strings.add("of");
        strings.add("string");

        extractAndPrint(strings);
        assertTrue(true);
    }

    @Test
    void treeSetTest() {
        Set<String> strings = new TreeSet<>();
        strings.add("it");
        strings.add("is");
        strings.add("array");
        strings.add("of");
        strings.add("string");

        extractAndPrint(strings);
        assertTrue(true);
    }

    @Test
    void hashSetTest() {
        Set<String> strings = new HashSet<>();
        strings.add("it");
        strings.add("is");
        strings.add("array");
        strings.add("of");
        strings.add("string");

        extractAndPrint(strings);
        assertTrue(true);
    }

    @Test
    void primitivesTest() {
        int i = 42;

        extractAndPrint(i);
        assertTrue(true);
    }

    private static void extractAndPrint(Object o) {
        Node extracted = ClassFieldExtractor.extract(o);
        print(extracted);
    }

    private static void print(@NotNull Node node) {
        Set<Object> visited = new HashSet<>();
        print(node, 0, visited);
    }

    private static void print(@NotNull Node node, int indent, @NotNull Set<Object> visited) {
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
