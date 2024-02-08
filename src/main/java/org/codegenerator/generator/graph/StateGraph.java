package org.codegenerator.generator.graph;

import com.rits.cloning.Cloner;
import kotlin.Triple;
import org.codegenerator.Utils;
import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.codegenerator.generator.graph.edges.EdgeGenerator;
import org.codegenerator.generator.graph.edges.EdgeMethod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class StateGraph {
    private final EdgeGenerator edgeGenerator = new EdgeGenerator();
    private final Cloner cloner = new Cloner();

    public @NotNull Path findPath(
            @NotNull AssignableTypePropertyGrouper assignableTypePropertyGrouper,
            @NotNull Supplier<Object> constructor,
            @NotNull UnaryOperator<Object> termination
    ) {
        Object beginObject = constructor.get();
        Class<?> clazz = beginObject.getClass();
        UnaryOperator<Object> copyObject = copyObject();
        Object finalObject = assignableTypePropertyGrouper.getObject();
        Map<Class<?>, List<Object>> typeToValues = assignableTypePropertyGrouper.get();

        List<EdgeMethod> edgeMethods = edgeGenerator.generate(clazz.getMethods(), typeToValues);

        Node finalNode = ClassFieldExtractor.extract(finalObject);
        Object beginObjectBuilt = termination.apply(beginObject);
        Triple<Object, Node, PathNode> triple = new Triple<>(beginObject, ClassFieldExtractor.extract(beginObjectBuilt), new PathNode(null, null, 0));
        triple = bfs(triple, finalNode, edgeMethods, copyObject, termination);

        PathNode finalPathNode = triple.getThird();
        Deque<EdgeMethod> path = new ArrayDeque<>();
        while (finalPathNode != null && finalPathNode.edgeMethod != null) {
            path.addFirst(finalPathNode.edgeMethod);
            finalPathNode = finalPathNode.prevPathNode;
        }

        return new Path(finalNode.diff(triple.getSecond()), new ArrayList<>(path));
    }

    public @NotNull Path findPath(
            @NotNull AssignableTypePropertyGrouper assignableTypePropertyGrouper,
            @NotNull Supplier<Object> constructor
    ) {
        return findPath(assignableTypePropertyGrouper, constructor, UnaryOperator.identity());
    }

    private @NotNull Triple<Object, Node, PathNode> bfs(
            @NotNull Triple<Object, Node, PathNode> triple,
            @NotNull Node finalNode,
            @NotNull List<EdgeMethod> edgeMethods,
            @NotNull UnaryOperator<Object> copyObject,
            @NotNull UnaryOperator<Object> termination
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
                } catch (Exception ignored) {
                    continue;
                }
                Object instanceBuilt = Utils.callSupplierWrapper(() -> termination.apply(instance));
                lowerLevel.add(new Triple<>(instance, ClassFieldExtractor.extract(instanceBuilt), new PathNode(prevPath, edgeMethod)));
            }
            List<Integer> diffs = lowerLevel.stream().map(t -> finalNode.diff(t.getSecond())).collect(Collectors.toList());
            int minDif = diffs.stream().min(Integer::compareTo).orElse(Integer.MAX_VALUE);
            queue = queue.stream().filter(t -> finalNode.diff(t.getSecond()) == minDif).collect(Collectors.toCollection(ArrayDeque::new));
            for (int i = 0; i < diffs.size(); i++) {
                if (minDif == diffs.get(i)) {
                    queue.add(lowerLevel.get(i));
                }
            }
        }
        return triple;
    }

    @Contract(pure = true)
    private @NotNull UnaryOperator<Object> copyObject() {
        return cloner::deepClone;
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
