package org.codegenerator.generator.graph;

import kotlin.Triple;
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

public class PojoStateGraph {
    private final Class<?> clazz;
    private final EdgeGeneratorMethod edgeGeneratorMethod;

    public PojoStateGraph(Class<?> clazz) {
        this.clazz = clazz;
        edgeGeneratorMethod = new EdgeGeneratorMethod(clazz);
    }

    public @NotNull List<EdgeMethod> findPath(
            @NotNull AssignableTypePropertyGrouper assignableTypePropertyGrouper,
            @NotNull Supplier<?> constructor,
            @NotNull Function<?, ?> termination
    ) {
        Object beginObject = constructor.get();
        Function<Object, Object> copyObject = copyObject(constructor);

        return findPath(beginObject, assignableTypePropertyGrouper, copyObject);
    }

    public @NotNull List<EdgeMethod> findPath(
            @NotNull AssignableTypePropertyGrouper assignableTypePropertyGrouper,
            @NotNull Supplier<?> constructor
    ) {
        return findPath(assignableTypePropertyGrouper, constructor, Function.identity());
    }

    private @NotNull List<EdgeMethod> findPath(
            Object beginObject,
            @NotNull AssignableTypePropertyGrouper assignableTypePropertyGrouper,
            Function<Object, Object> copyObject
    ) {
        List<EdgeMethod> edgeMethods = edgeGeneratorMethod.generate(assignableTypePropertyGrouper.get());
        Object finalObject = assignableTypePropertyGrouper.getObject();

        Node finalNode = ClassFieldExtractor.extract(finalObject);
        Triple<Object, Node, PathNode> triple = new Triple<>(beginObject, ClassFieldExtractor.extract(beginObject), new PathNode(null, null, 0));
        triple = bfs(triple, finalNode, edgeMethods, copyObject);
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
            Triple<Object, Node, PathNode> triple,
            Node finalNode,
            List<EdgeMethod> edgeMethods,
            Function<Object, Object> copyObject
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
                lowerLevel.add(new Triple<>(instance, ClassFieldExtractor.extract(instance), new PathNode(prevPath, edgeMethod)));
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
        return (o) -> {
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
