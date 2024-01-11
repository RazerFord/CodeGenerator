package org.codegenerator.generator.graph;

import kotlin.Triple;
import org.apache.commons.lang3.ClassUtils;
import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.codegenerator.generator.codegenerators.MethodCall;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.codegenerator.Utils.callSupplierWrapper;

public class StateGraph {
    private final Class<?> clazz;
    private final EdgeGenerator edgeGenerator;

    public StateGraph(Class<?> clazz) {
        this.clazz = clazz;
        edgeGenerator = new EdgeGenerator(clazz);
    }

    public @NotNull List<MethodCall> findPath(Object beginObject, Object finalObject, Function<Object, Object> copyObject) {
        List<Edge> edges = edgeGenerator.generate(prepareTypeToValues(finalObject));
        return findPath(beginObject, finalObject, edges, copyObject);
    }

    private @NotNull List<MethodCall> findPath(Object beginObject, Object finalObject, List<Edge> edges, Function<Object, Object> copyObject) {
        Node finalNode = ClassFieldExtractor.extract(finalObject);
        Triple<Object, Node, PathNode> triple = new Triple<>(beginObject, ClassFieldExtractor.extract(beginObject), new PathNode(null, null, 0));
        triple = bfs(triple, finalNode, edges, copyObject);
        if (triple == null) {
            return Collections.emptyList();
        }

        PathNode finalPathNode = triple.getThird();
        Deque<Edge> path = new ArrayDeque<>();
        while (finalPathNode != null && finalPathNode.edge != null) {
            path.addFirst(finalPathNode.edge);
            finalPathNode = finalPathNode.prevPathNode;
        }

        return path.stream().map(e -> new MethodCall(e.getMethod(), e.getArgs())).collect(Collectors.toList());
    }

    private @Nullable Triple<Object, Node, PathNode> bfs(Triple<Object, Node, PathNode> triple, Node finalNode, List<Edge> edges, Function<Object, Object> copyObject) {
        Queue<Triple<Object, Node, PathNode>> queue = new ArrayDeque<>(Collections.singleton(triple));
        Set<Node> visited = new HashSet<>(Collections.singleton(finalNode));

        while (!queue.isEmpty()) {
            triple = queue.poll();

            Node curNode = triple.getSecond();
            PathNode prevPath = triple.getThird();

            if (curNode.equals(finalNode)) {
                return triple;
            }
            if (visited.contains(curNode)) {
                continue;
            }
            visited.add(curNode);

            List<Triple<Object, Node, PathNode>> lowerLevel = new ArrayList<>();

            for (Edge edge : edges) {
                Object instance = copyObject.apply(triple.getFirst());
                edge.invoke(instance);
                lowerLevel.add(new Triple<>(instance, ClassFieldExtractor.extract(instance), new PathNode(prevPath, edge)));
            }
            List<Integer> diffs = lowerLevel.stream().map(t -> finalNode.diff(t.getSecond())).collect(Collectors.toList());
            int minDif = diffs.stream().min(Integer::compareTo).orElse(Integer.MAX_VALUE);
            for (int i = 0; i < diffs.size(); i++) {
                if (minDif == diffs.get(i)) {
                    queue.add(lowerLevel.get(i));
                }
            }
        }
        return null;
    }

    private @NotNull Map<Class<?>, List<Object>> prepareTypeToValues(Object o) {
        Map<Class<?>, List<Object>> typeToValues = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            List<Object> list = typeToValues.computeIfAbsent(field.getType(), k -> new ArrayList<>());
            field.setAccessible(true);
            list.add(callSupplierWrapper(() -> field.get(o)));
        }
        mergeValuesOfSameTypes(typeToValues);
        return typeToValues;
    }

    @Contract(pure = true)
    private void mergeValuesOfSameTypes(@NotNull Map<Class<?>, List<Object>> typeToValues) {
        for (Class<?> type : typeToValues.keySet()) {
            for (Map.Entry<Class<?>, List<Object>> entry : typeToValues.entrySet()) {
                if (ClassUtils.isAssignable(entry.getKey(), type)) {
                    List<Object> list = typeToValues.get(type);
                    Set<Object> set = new HashSet<>(list);
                    set.addAll(entry.getValue());
                    list.clear();
                    list.addAll(set);
                }
            }
        }
    }

    private static final class PathNode {
        private final PathNode prevPathNode;
        private final Edge edge;
        private final int depth;

        @Contract(pure = true)
        private PathNode(@NotNull PathNode prevPathNode, Edge edge) {
            this.prevPathNode = prevPathNode;
            this.edge = edge;
            depth = prevPathNode.depth + 1;
        }

        @Contract(pure = true)
        private PathNode(PathNode prevPathNode, Edge edge, int depth) {
            this.prevPathNode = prevPathNode;
            this.edge = edge;
            this.depth = depth;
        }
    }
}
