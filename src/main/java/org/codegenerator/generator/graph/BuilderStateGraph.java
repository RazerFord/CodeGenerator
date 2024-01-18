package org.codegenerator.generator.graph;

import kotlin.Triple;
import org.codegenerator.Utils;
import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.codegenerator.Utils.callRunnableWrapper;
import static org.codegenerator.Utils.callSupplierWrapper;

public class BuilderStateGraph {
    private final Class<?> builderClazz;
    private final Supplier<?> builderConstructor;
    private final Method builderMethodBuild;
    private final EdgeGeneratorMethod edgeGeneratorMethod;

    public BuilderStateGraph(Class<?> builderClazz, Supplier<?> builderConstructor, Method builderMethodBuild) {
        this.builderClazz = builderClazz;
        this.builderConstructor = builderConstructor;
        this.builderMethodBuild = builderMethodBuild;
        edgeGeneratorMethod = new EdgeGeneratorMethod(builderClazz);
    }

    public @NotNull Deque<EdgeMethod> findPath(Object finalObject) {
        AssignableTypePropertyGrouper assignableTypePropertyGrouper = new AssignableTypePropertyGrouper(finalObject);
        @NotNull Map<Class<?>, List<Object>> values = assignableTypePropertyGrouper.get();

        Object beginObject = builderConstructor.get();
        Function<Object, Object> copyObject = copyObject(builderConstructor);

        List<EdgeMethod> edgeMethods = edgeGeneratorMethod.generate(values);
        return findPath(beginObject, finalObject, edgeMethods, copyObject);
    }

    private @NotNull Deque<EdgeMethod> findPath(Object beginObject, Object finalObject, List<EdgeMethod> edgeMethods, Function<Object, Object> copyObject) {
        Node finalNode = ClassFieldExtractor.extract(finalObject);
        Object beginObjectBuilt = Utils.callSupplierWrapper(() -> builderMethodBuild.invoke(beginObject));
        Triple<Object, Node, PathNode> triple = new Triple<>(beginObject, ClassFieldExtractor.extract(beginObjectBuilt), new PathNode(null, null, 0));
        triple = bfs(triple, finalNode, edgeMethods, copyObject);
        if (triple == null) {
            return new ArrayDeque<>();
        }

        PathNode finalPathNode = triple.getThird();
        Deque<EdgeMethod> path = new ArrayDeque<>();
        while (finalPathNode != null && finalPathNode.edgeMethod != null) {
            path.addFirst(finalPathNode.edgeMethod);
            finalPathNode = finalPathNode.prevPathNode;
        }

        return path;
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
                Object instanceBuilt = Utils.callSupplierWrapper(() -> builderMethodBuild.invoke(instance));
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
        return (o) -> {
            Object instance = callSupplierWrapper(supplier::get);
            for (Field field : builderClazz.getDeclaredFields()) {
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
