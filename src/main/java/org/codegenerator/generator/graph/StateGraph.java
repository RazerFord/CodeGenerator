package org.codegenerator.generator.graph;

import kotlin.Triple;
import org.codegenerator.Utils;
import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.codegenerator.Utils.callRunnableWrapper;
import static org.codegenerator.Utils.callSupplierWrapper;

public class StateGraph {
    private final Class<?> clazz;
    private final EdgeGeneratorMethod edgeGeneratorMethod;

    public StateGraph(Class<?> clazz) {
        this.clazz = clazz;
        edgeGeneratorMethod = new EdgeGeneratorMethod(clazz);
    }

    public @NotNull List<EdgeMethod> findPath(
            @NotNull AssignableTypePropertyGrouper assignableTypePropertyGrouper,
            @NotNull Supplier<Object> constructor,
            @NotNull Function<Object, Object> termination
    ) {
        Object beginObject = constructor.get();
        Function<Object, Object> copyObject = copyObject(constructor);
        Object finalObject = assignableTypePropertyGrouper.getObject();
        Map<Class<?>, List<Object>> typeToValues = assignableTypePropertyGrouper.get();

        return findPath(beginObject, finalObject, typeToValues, copyObject, termination);
    }

    public @NotNull List<EdgeMethod> findPath(
            @NotNull AssignableTypePropertyGrouper assignableTypePropertyGrouper,
            @NotNull Supplier<Object> constructor
    ) {
        return findPath(assignableTypePropertyGrouper, constructor, Function.identity());
    }

    public @NotNull List<EdgeMethod> findPath(
            @NotNull Object beginObject,
            @NotNull Object finalObject,
            @NotNull Map<Class<?>, List<Object>> typeToValues,
            @NotNull Function<Object, Object> copyObject,
            @NotNull Function<Object, Object> termination
    ) {
        List<EdgeMethod> edgeMethods = edgeGeneratorMethod.generate(typeToValues);

        Node finalNode = ClassFieldExtractor.extract(finalObject);
        Object beginObjectBuilt = termination.apply(beginObject);
        Triple<Object, Node, PathNode> triple = new Triple<>(beginObject, ClassFieldExtractor.extract(beginObjectBuilt), new PathNode(null, null, 0));
        triple = bfs(triple, finalNode, edgeMethods, copyObject, termination);
        if (triple == null) {
            return Collections.emptyList();
        }

        PathNode finalPathNode = triple.getThird();
        Deque<EdgeMethod> path = new ArrayDeque<>();
        while (finalPathNode != null && finalPathNode.edgeMethod != null) {
            path.addFirst(finalPathNode.edgeMethod);
            finalPathNode = finalPathNode.prevPathNode;
        }

        return new ArrayList<>(path);
    }


    private @Nullable Triple<Object, Node, PathNode> bfs(
            @NotNull Triple<Object, Node, PathNode> triple,
            @NotNull Node finalNode,
            @NotNull List<EdgeMethod> edgeMethods,
            @NotNull Function<Object, Object> copyObject,
            @NotNull Function<Object, Object> termination
    ) {
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

            for (EdgeMethod edgeMethod : edgeMethods) {
                Object instance = copyObject.apply(triple.getFirst());
                try {
                    edgeMethod.invoke(instance);
                } catch (Throwable ignored) {
                    continue;
                }
                Object instanceBuilt = Utils.callSupplierWrapper(() -> termination.apply(instance));
                lowerLevel.add(new Triple<>(instance, ClassFieldExtractor.extract(instanceBuilt), new PathNode(prevPath, edgeMethod)));
            }
            List<Integer> diffs = lowerLevel.stream().map(t -> finalNode.diff(t.getSecond())).collect(Collectors.toList());
            int minDif = diffs.stream().min(Integer::compareTo).orElse(Integer.MAX_VALUE);
            queue = queue.stream().filter(t -> t.getSecond().diff(finalNode) == minDif).collect(Collectors.toCollection(ArrayDeque::new));
            for (int i = 0; i < diffs.size(); i++) {
                if (minDif == diffs.get(i)) {
                    queue.add(lowerLevel.get(i));
                }
            }
        }
        return null;
    }

    @Contract(pure = true)
    private @NotNull Function<Object, Object> copyObject(@NotNull Supplier<?> supplier) {
        return o -> {
            Object instance = callSupplierWrapper(supplier::get);
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                callRunnableWrapper(() -> field.set(instance, callSupplierWrapper(() -> field.get(o))));
            }
            return instance;
        };
    }

    private static final class PathNode {
        private final PathNode prevPathNode;
        private final EdgeMethod edgeMethod;
        private final int depth;

        @Contract(pure = true)
        private PathNode(@NotNull PathNode prevPathNode, EdgeMethod edgeMethod) {
            this.prevPathNode = prevPathNode;
            this.edgeMethod = edgeMethod;
            depth = prevPathNode.depth + 1;
        }

        @Contract(pure = true)
        private PathNode(PathNode prevPathNode, EdgeMethod edgeMethod, int depth) {
            this.prevPathNode = prevPathNode;
            this.edgeMethod = edgeMethod;
            this.depth = depth;
        }
    }
}
