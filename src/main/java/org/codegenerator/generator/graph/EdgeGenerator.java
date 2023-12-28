package org.codegenerator.generator.graph;

import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;

public class EdgeGenerator {
    private final Class<?> clazz;

    public EdgeGenerator(@NotNull Class<?> clazz) {
        this.clazz = clazz;
    }

    public List<Edge> generate(Map<Class<?>, List<Object>> typeToValues) {
        typeToValues = new HashMap<>(typeToValues);
        List<Edge> edges = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            List<Node> roots = buildGraph(method, typeToValues);
            List<List<Node>> listArguments = generatePossibleArguments(roots);
            for (List<Node> arguments : listArguments) {
                edges.add(new Edge(method, extractArgs(arguments, typeToValues)));
            }
        }
        return edges;
    }

    private @NotNull List<List<Node>> generatePossibleArguments(@NotNull List<Node> roots) {
        List<List<Node>> results = new ArrayList<>();
        for (Node root : roots) {
            recursiveTraverse(root, new ArrayDeque<>(Collections.singleton(root)), results);
        }
        return results;
    }

    private void recursiveTraverse(@NotNull Node root, Deque<Node> currPath, List<List<Node>> results) {
        for (Node child : root.nodes) {
            currPath.addLast(child);
            recursiveTraverse(child, currPath, results);
        }
        if (root.nodes.isEmpty()) {
            results.add(new ArrayList<>(currPath));
        }
        currPath.removeLast();
    }

    private Object @NotNull [] extractArgs(@NotNull List<Node> arguments, Map<Class<?>, List<Object>> typeToValues) {
        Object[] args = new Object[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            args[i] = typeToValues.get(arguments.get(i).type).get(arguments.get(i).i);
        }
        return args;
    }

    private @NotNull List<Node> buildGraph(@NotNull Method method, Map<Class<?>, List<Object>> typeToValues) {
        List<List<Node>> levels = new ArrayList<>();

        for (Class<?> type : method.getParameterTypes()) {
            List<Node> level = new ArrayList<>();
            List<Object> values = typeToValues.computeIfAbsent(type, k -> computeValues(k, typeToValues));
            for (int i = 0; i < values.size(); i++) {
                level.add(new Node(type, i));
            }
            if (level.size() != method.getParameterCount()) {
                return Collections.emptyList();
            }
            levels.add(level);
        }
        drawEdges(levels);

        return levels.isEmpty() ? Collections.emptyList() : levels.get(0);
    }

    private void drawEdges(@NotNull List<List<Node>> levels) {
        for (int i = 0; i < levels.size() - 1; i++) {
            List<Node> prev = levels.get(i);
            List<Node> next = levels.get(i + 1);
            for (Node prevNode : prev) {
                for (Node nextNode : next) {
                    prevNode.addNode(nextNode);
                }
            }
        }
    }

    private List<Object> computeValues(Class<?> nKey, @NotNull Map<Class<?>, List<Object>> map) {
        for (Class<?> key : map.keySet()) {
            if (ClassUtils.isAssignable(nKey, key)) {
                return map.get(key);
            }
        }
        return Collections.emptyList();
    }

    private static final class Node {
        private final List<Node> nodes = new ArrayList<>();
        private final Class<?> type;
        private final int i;

        private Node() {
            this(Object.class, -1);
        }

        private Node(Class<?> type, int i) {
            this.type = type;
            this.i = i;
        }

        @SuppressWarnings("UnusedReturnValue")
        private boolean addNode(Node node) {
            return nodes.add(node);
        }
    }
}
