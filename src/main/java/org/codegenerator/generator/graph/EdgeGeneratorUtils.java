package org.codegenerator.generator.graph;

import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.*;

public class EdgeGeneratorUtils {

    public static @NotNull List<List<Node>> generatePossibleArguments(@NotNull List<Node> roots) {
        List<List<Node>> results = new ArrayList<>();
        for (Node root : roots) {
            recursiveTraverse(root, new ArrayDeque<>(Collections.singleton(root)), results);
        }
        return results;
    }

    public static void recursiveTraverse(@NotNull Node root, Deque<Node> currPath, List<List<Node>> results) {
        for (Node child : root.getNodes()) {
            currPath.addLast(child);
            recursiveTraverse(child, currPath, results);
        }
        if (root.getNodes().isEmpty()) {
            results.add(new ArrayList<>(currPath));
        }
        currPath.removeLast();
    }

    public static Object @NotNull [] extractArgs(@NotNull List<Node> arguments, Map<Class<?>, List<Object>> typeToValues) {
        Object[] args = new Object[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            args[i] = typeToValues.get(arguments.get(i).getType()).get(arguments.get(i).getI());
        }
        return args;
    }

    /*
     * This method builds a graph
     *
     * Let the type of the method be `void(int, int)`, and the map contains a pair [int => {1, 2, 3}].
     * Then the graph will be built:
     *
     * (int, 0) ---> (int, 0)
     *           |
     * (int, 1) ---> (int, 1)
     *           |
     * (int, 2) ---> (int, 2)
     *
     * @param method that can be executed. Usually this is an example `Constructor<?>` or `Method`
     * @param map of types to their value
     * @return returns a list of starting vertices of the graph
     */
    public static <T extends Executable> @NotNull List<Node> buildGraph(@NotNull T executable, Map<Class<?>, List<Object>> typeToValues) {
        List<List<Node>> levels = new ArrayList<>();

        for (Class<?> type : executable.getParameterTypes()) {
            List<Node> level = new ArrayList<>();
            List<Object> values = typeToValues.computeIfAbsent(type, k -> computeValues(k, typeToValues));
            for (int i = 0; i < values.size(); i++) {
                level.add(new Node(type, i));
            }
            levels.add(level);
        }
        drawEdges(levels);

        if (levels.size() != executable.getParameterCount() || levels.isEmpty()) {
            return Collections.emptyList();
        }
        return levels.get(0);
    }

    public static void drawEdges(@NotNull List<List<Node>> levels) {
        for (int i = 0; i < levels.size() - 1; i++) {
            List<Node> prev = levels.get(i);
            List<Node> next = levels.get(i + 1);
            for (Node prevNode : prev) {
                prevNode.getNodes().addAll(next);
            }
        }
    }

    public static List<Object> computeValues(Class<?> nKey, @NotNull Map<Class<?>, List<Object>> map) {
        for (Class<?> key : map.keySet()) {
            if (ClassUtils.isAssignable(nKey, key)) {
                return map.get(key);
            }
        }
        return Collections.emptyList();
    }

    public static final class Node {
        private final List<Node> nodes = new ArrayList<>();
        private final Class<?> type;
        private final int i;

        public Node(Class<?> type, int i) {
            this.type = type;
            this.i = i;
        }

        public List<Node> getNodes() {
            return nodes;
        }

        public Class<?> getType() {
            return type;
        }

        public int getI() {
            return i;
        }
    }
}
