package org.codegenerator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import org.codegenerator.generator.Generator;
import org.codegenerator.generator.Generators;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;

import static org.codegenerator.Common.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CollectionTest {
    ///////////////////////// Testing List Interface Implementers /////////////////////////
    @Test
    void smallArrayListTest() throws IOException {
        final String generatedClassName = "GeneratedSmallArrayListClass";
        doTestList(generatedClassName, ArrayList::new, 10);
    }

    @Test
    void smallLinkedListTest() throws IOException {
        final String generatedClassName = "GeneratedSmallLinkedListClass";
        doTestList(generatedClassName, LinkedList::new, 10);
    }

    @Test
    void smallVectorTest() throws IOException {
        final String generatedClassName = "GeneratedSmallVectorClass";
        doTestList(generatedClassName, Vector::new, 10);
    }

    @Test
    void smallStackTest() throws IOException {
        final String generatedClassName = "GeneratedSmallStackClass";
        doTestList(generatedClassName, Stack::new, 10);
    }

    @Test
    @Disabled("This functionality is not supported")
    void mediumArrayListTest() throws IOException {
        final String generatedClassName = "GeneratedMediumArrayListClass";
        doTestList(generatedClassName, ArrayList::new, 1_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void mediumLinkedListTest() throws IOException {
        final String generatedClassName = "GeneratedMediumLinkedListClass";
        doTestList(generatedClassName, LinkedList::new, 1_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void mediumVectorTest() throws IOException {
        final String generatedClassName = "GeneratedMediumVectorClass";
        doTestList(generatedClassName, Vector::new, 1_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void mediumStackTest() throws IOException {
        final String generatedClassName = "GeneratedMediumStackClass";
        doTestList(generatedClassName, Stack::new, 1_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void largeArrayListTest() throws IOException {
        final String generatedClassName = "GeneratedLargeArrayListClass";
        doTestList(generatedClassName, ArrayList::new, 1_000_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void largeLinkedListTest() throws IOException {
        final String generatedClassName = "GeneratedLargeLinkedListClass";
        doTestList(generatedClassName, LinkedList::new, 1_000_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void largeVectorTest() throws IOException {
        final String generatedClassName = "GeneratedLargeVectorClass";
        doTestList(generatedClassName, Vector::new, 1_000_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void largeStackTest() throws IOException {
        final String generatedClassName = "GeneratedLargeStackClass";
        doTestList(generatedClassName, Stack::new, 1_000_000);
    }

    ///////////////////////// Testing Queue Interface Implementers /////////////////////////
    @Test
    void smallArrayDequeTest() throws IOException {
        final String generatedClassName = "GeneratedSmallArrayDequeClass";
        doTestQueue(generatedClassName, ArrayDeque::new, 10);
    }

    @Test
    void smallPriorityQueueTest() throws IOException {
        final String generatedClassName = "GeneratedSmallPriorityQueueClass";
        doTestQueue(generatedClassName, PriorityQueue::new, 10);
    }

    @Test
    @Disabled("This functionality is not supported")
    void mediumArrayDequeTest() throws IOException {
        final String generatedClassName = "GeneratedMediumArrayDequeClass";
        doTestQueue(generatedClassName, ArrayDeque::new, 1_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void mediumPriorityQueueTest() throws IOException {
        final String generatedClassName = "GeneratedMediumPriorityQueueClass";
        doTestQueue(generatedClassName, PriorityQueue::new, 1_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void largeArrayDequeTest() throws IOException {
        final String generatedClassName = "GeneratedLargeArrayDequeClass";
        doTestQueue(generatedClassName, ArrayDeque::new, 1_000_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void largePriorityQueueTest() throws IOException {
        final String generatedClassName = "GeneratedLargePriorityQueueClass";
        doTestQueue(generatedClassName, PriorityQueue::new, 1_000_000);
    }

    ///////////////////////// Testing Set Interface Implementers /////////////////////////
    @Test
    void smallHashSetDequeTest() throws IOException {
        final String generatedClassName = "GeneratedSmallHashSetClass";
        doTestSet(generatedClassName, HashSet::new, 10);
    }

    @Test
    void smallLinkedHashSetTest() throws IOException {
        final String generatedClassName = "GeneratedSmallLinkedHashSetClass";
        doTestSet(generatedClassName, LinkedHashSet::new, 10);
    }

    @Test
    void smallTreeSetTest() throws IOException {
        final String generatedClassName = "GeneratedSmallTreeSetClass";
        doTestSet(generatedClassName, TreeSet::new, 10);
    }

    @Test
    @Disabled("This functionality is not supported")
    void mediumHashSetDequeTest() throws IOException {
        final String generatedClassName = "GeneratedMediumHashSetClass";
        doTestSet(generatedClassName, HashSet::new, 1_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void mediumLinkedHashSetTest() throws IOException {
        final String generatedClassName = "GeneratedMediumLinkedHashSetClass";
        doTestSet(generatedClassName, LinkedHashSet::new, 1_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void mediumTreeSetTest() throws IOException {
        final String generatedClassName = "GeneratedMediumTreeSetClass";
        doTestSet(generatedClassName, TreeSet::new, 1_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void largeHashSetDequeTest() throws IOException {
        final String generatedClassName = "GeneratedLargeHashSetClass";
        doTestSet(generatedClassName, HashSet::new, 1_000_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void largeLinkedHashSetTest() throws IOException {
        final String generatedClassName = "GeneratedLargeLinkedHashSetClass";
        doTestSet(generatedClassName, LinkedHashSet::new, 1_000_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void largeTreeSetTest() throws IOException {
        final String generatedClassName = "GeneratedLargeTreeSetClass";
        doTestSet(generatedClassName, TreeSet::new, 1_000_000);
    }

    ///////////////////////// Testing Map Interface Implementers /////////////////////////
    @Test
    void smallHashTableTest() throws IOException {
        final String generatedClassName = "GeneratedSmallHashTableClass";
        doTestMap(generatedClassName, Hashtable::new, 10);
    }

    @Test
    void smallLinkedHashMapTest() throws IOException {
        final String generatedClassName = "GeneratedSmallLinkedHashMapClass";
        doTestMap(generatedClassName, LinkedHashMap::new, 10);
    }

    @Test
    void smallHashMapTest() throws IOException {
        final String generatedClassName = "GeneratedSmallHashMapClass";
        doTestMap(generatedClassName, HashMap::new, 10);
    }

    @Test
    void smallTreeMapTest() throws IOException {
        final String generatedClassName = "GeneratedSmallTreeMapClass";
        doTestMap(generatedClassName, TreeMap::new, 10);
    }

    @Test
    @Disabled("This functionality is not supported")
    void mediumHashTableTest() throws IOException {
        final String generatedClassName = "GeneratedMediumHashTableClass";
        doTestMap(generatedClassName, Hashtable::new, 1_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void mediumLinkedHashMapTest() throws IOException {
        final String generatedClassName = "GeneratedMediumLinkedHashMapClass";
        doTestMap(generatedClassName, LinkedHashMap::new, 1_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void mediumHashMapTest() throws IOException {
        final String generatedClassName = "GeneratedMediumHashMapClass";
        doTestMap(generatedClassName, HashMap::new, 1_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void mediumTreeMapTest() throws IOException {
        final String generatedClassName = "GeneratedMediumTreeMapClass";
        doTestMap(generatedClassName, TreeMap::new, 1_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void bigHashTableTest() throws IOException {
        final String generatedClassName = "GeneratedBigHashTableClass";
        doTestMap(generatedClassName, Hashtable::new, 1_000_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void bigLinkedHashMapTest() throws IOException {
        final String generatedClassName = "GeneratedBigLinkedHashMapClass";
        doTestMap(generatedClassName, LinkedHashMap::new, 1_000_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void bigHashMapTest() throws IOException {
        final String generatedClassName = "GeneratedBigHashMapClass";
        doTestMap(generatedClassName, HashMap::new, 1_000_000);
    }

    @Test
    @Disabled("This functionality is not supported")
    void bigTreeMapTest() throws IOException {
        final String generatedClassName = "GeneratedBigTreeMapClass";
        doTestMap(generatedClassName, TreeMap::new, 1_000_000);
    }

    ///////////////////////// Testing Multimap Guava /////////////////////////
    @Test
    void smallMultimapTest() throws IOException {
        final String generatedClassName = "GeneratedSmallMultimapClass";
        doTestMultimap(generatedClassName, ArrayListMultimap::create, 10, 5);
    }

    @Test
    @Disabled("This functionality is not supported")
    void mediumMultimapTest() throws IOException {
        final String generatedClassName = "GeneratedMediumLinkedHashMapClass";
        doTestMultimap(generatedClassName, ArrayListMultimap::create, 1_000, 500);
    }

    @Test
    @Disabled("This functionality is not supported")
    void largeMultimapTest() throws IOException {
        final String generatedClassName = "GeneratedLargeMultimapClass";
        doTestMultimap(generatedClassName, ArrayListMultimap::create, 1_000_000, 500_000);
    }

    private static void doTestList(
            String generatedClassName,
            Supplier<List<Integer>> supplier,
            int size
    ) throws IOException {
        Generator generator = Generators.standard(PACKAGE_NAME, generatedClassName, METHOD_NAME);

        List<Integer> list = toFillCollectionInteger(supplier, size, 42);
        generator.generateCode(list, Paths.get(OUTPUT_DIRECTORY));

        List<Integer> other = createObject(generatedClassName);
        assertEquals(list, other);
    }

    private static void doTestQueue(
            String generatedClassName,
            Supplier<Queue<Integer>> supplier,
            int size
    ) throws IOException {
        Generator generator = Generators.standard(PACKAGE_NAME, generatedClassName, METHOD_NAME);

        Queue<Integer> queue = toFillCollectionInteger(supplier, size, 42);
        generator.generateCode(queue, Paths.get(OUTPUT_DIRECTORY));

        Queue<Integer> other = createObject(generatedClassName);
        for (
                Integer elemQueue = queue.poll(), elemOther = other.poll();
                elemQueue != null || elemOther != null;
                elemQueue = queue.poll(), elemOther = other.poll()
        ) {
            assertEquals(elemQueue, elemOther);
        }
    }

    private static void doTestSet(
            String generatedClassName,
            Supplier<Set<Integer>> supplier,
            int size
    ) throws IOException {
        Generator generator = Generators.standard(PACKAGE_NAME, generatedClassName, METHOD_NAME);

        Set<Integer> queue = toFillCollectionInteger(supplier, size, 42);
        generator.generateCode(queue, Paths.get(OUTPUT_DIRECTORY));

        Set<Integer> other = createObject(generatedClassName);
        assertEquals(queue, other);
    }

    private static void doTestMap(
            String generatedClassName,
            Supplier<Map<Integer, Integer>> supplier,
            int size
    ) throws IOException {
        Generator generator = Generators.standard(PACKAGE_NAME, generatedClassName, METHOD_NAME);

        Map<Integer, Integer> queue = toFillMapInteger(supplier, size, 42);
        generator.generateCode(queue, Paths.get(OUTPUT_DIRECTORY));

        Map<Integer, Integer> other = createObject(generatedClassName);
        assertEquals(queue, other);
    }

    private static void doTestMultimap(
            String generatedClassName,
            Supplier<Multimap<Integer, Integer>> supplier,
            int size,
            int maxIdx
    ) throws IOException {
        Generator generator = Generators.standard(PACKAGE_NAME, generatedClassName, METHOD_NAME);

        Multimap<Integer, Integer> queue = toFillMultimapInteger(supplier, size, maxIdx, 42);
        generator.generateCode(queue, Paths.get(OUTPUT_DIRECTORY));

        ListMultimap<Integer, Integer> other = createObject(generatedClassName);
        assertEquals(queue, other);
    }

    private static <T extends Collection<Integer>> T toFillCollectionInteger(
            @NotNull Supplier<T> supplier,
            int size,
            int seed
    ) {
        T list = supplier.get();
        Random rnd = new Random(seed);
        for (int i = 0; i < size; i++) {
            list.add(rnd.nextInt());
        }
        return list;
    }

    private static <T extends Map<Integer, Integer>> T toFillMapInteger(
            @NotNull Supplier<T> supplier,
            int size,
            int seed
    ) {
        T list = supplier.get();
        Random rnd = new Random(seed);
        for (int i = 0; i < size; i++) {
            list.put(i, rnd.nextInt());
        }
        return list;
    }

    private static <T extends Multimap<Integer, Integer>> T toFillMultimapInteger(
            @NotNull Supplier<T> supplier,
            int size,
            int maxIdx,
            int seed
    ) {
        T list = supplier.get();
        Random rnd = new Random(seed);
        for (int i = 0; i < size; i++) {
            list.put(i % maxIdx, rnd.nextInt());
        }
        return list;
    }
}
