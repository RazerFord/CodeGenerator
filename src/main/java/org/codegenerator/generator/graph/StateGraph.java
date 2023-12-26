package org.codegenerator.generator.graph;

import kotlin.Triple;
import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.codegenerator.generator.MethodCall;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.codegenerator.Utils.*;

public class StateGraph {
    private final Method[] methods;
    private final Map<Integer, List<List<Integer>>> combinationsWithPermutations;

    public StateGraph(@NotNull Class<?> clazz) {
        methods = clazz.getDeclaredMethods();
        int maxArguments = Arrays.stream(clazz.getDeclaredMethods()).filter(it -> Modifier.isPublic(it.getModifiers())).map(Method::getParameterCount).max(Comparator.naturalOrder()).orElse(0);
        combinationsWithPermutations = generateCombinationsWithPermutations(clazz.getDeclaredFields().length, maxArguments);
    }

    public @NotNull List<MethodCall> findPath(Object beginObject, Object finalObject, Function<Object, Object> copyObject) {
        Node finalNode = ClassFieldExtractor.extract(finalObject);
        Triple<Object, Node, PathNode> triple = new Triple<>(beginObject, ClassFieldExtractor.extract(beginObject), new PathNode(null, null, 0));
        triple = bfs(triple, finalNode, copyObject);
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

    private @Nullable Triple<Object, Node, PathNode> bfs(Triple<Object, Node, PathNode> triple, Node finalNode, Function<Object, Object> copyObject) {
        Queue<Triple<Object, Node, PathNode>> queue = new ArrayDeque<>(Collections.singleton(triple));
        Set<Node> visited = new HashSet<>(Collections.singleton(finalNode));
        List<Edge> edges = generateEdges(finalNode);

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

    private @NotNull List<Edge> generateEdges(@NotNull Node node) {
        List<Edge> edges = new ArrayList<>();
        List<Map.Entry<Object, Node>> entries = new ArrayList<>(node.entrySet());

        for (Method method : methods) {
            edges.addAll(generateEdges(entries, method));
        }
        return edges;
    }

    private @NotNull List<Edge> generateEdges(@NotNull List<Map.Entry<Object, Node>> values, @NotNull Method method) {
        List<Edge> edges = new ArrayList<>();
        List<List<Integer>> sequences = combinationsWithPermutations.getOrDefault(method.getParameterCount(), Collections.emptyList());
        Class<?>[] argsTypes = new Class[method.getParameterCount()];
        for (List<Integer> sequence : sequences) {
            Object[] args = new Object[method.getParameterCount()];
            int j = 0;
            for (int i : sequence) {
                argsTypes[j] = ((Field) values.get(i).getKey()).getType();
                args[j++] = values.get(i).getValue().getValue();
            }
            if (equalsArgs(argsTypes, method.getParameterTypes())) {
                edges.add(new Edge(method, args));
            }
        }
        return edges;
    }

    @Contract(pure = true)
    private boolean equalsArgs(Class<?> @NotNull [] l, Class<?> @NotNull [] r) {
        if (l.length != r.length) {
            return false;
        }
        for (int i = 0; i < l.length; i++) {
            if (l[i] != r[i]) {
                return false;
            }
        }
        return true;
    }

    private static Map<Integer, List<List<Integer>>> generateCombinationsWithPermutations(int numberProperties, int maxArguments) {
        List<Integer> sequence = new ArrayList<>(numberProperties);
        for (int i = 0; i < numberProperties; i++) {
            sequence.add(i);
        }
        return combinationsWithPermutations(sequence, maxArguments).stream()
                .collect(Collectors.groupingBy(List::size, Collectors.toList()));
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
