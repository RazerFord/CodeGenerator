package org.codegenerator.generator.graph;

import com.rits.cloning.Cloner;
import kotlin.Triple;
import org.codegenerator.ClonerUtilities;
import org.codegenerator.CustomLogger;
import org.codegenerator.Utils;
import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.codegenerator.generator.TargetObject;
import org.codegenerator.generator.graph.edges.EdgeGenerator;
import org.codegenerator.generator.graph.edges.EdgeMethod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LazyGraph {
    private static final Logger LOGGER = CustomLogger.getLogger();

    private final EdgeGenerator edgeGenerator = new EdgeGenerator();
    private final Cloner cloner = ClonerUtilities.standard();

    public @NotNull Path findPath(
            @NotNull TargetObject targetObject,
            @NotNull Supplier<Object> constructor,
            @NotNull UnaryOperator<Object> termination
    ) {
        Object beginObject = constructor.get();
        return findPath(targetObject, beginObject, termination);
    }

    public @NotNull Path findPath(
            @NotNull TargetObject targetObject,
            @NotNull Object beginObject,
            @NotNull UnaryOperator<Object> termination
    ) {
        Class<?> clazz = beginObject.getClass();
        UnaryOperator<Object> copyObject = copyObject();
        Object finalObject = targetObject.getObject();

        List<EdgeMethod> methods = edgeGenerator.generate(clazz.getMethods(), targetObject.get());

        Node finalNode = ClassFieldExtractor.extract(finalObject);
        Object beginObjectBuilt = termination.apply(beginObject);
        Triple<Object, Node, PathNode> triple = new Triple<>(beginObject, ClassFieldExtractor.extract(beginObjectBuilt), new PathNode(null, null, 0));
        triple = bfs(triple, finalNode, methods, copyObject, termination);

        PathNode finalPathNode = triple.getThird();
        Deque<EdgeMethod> path = new ArrayDeque<>();
        while (finalPathNode != null && finalPathNode.edgeMethod != null) {
            path.addFirst(finalPathNode.edgeMethod);
            finalPathNode = finalPathNode.prevPathNode;
        }

        return new Path(
                triple.getFirst(),
                finalNode.diff(triple.getSecond()),
                new ArrayList<>(path)
        );
    }

    public @NotNull Path findPath(
            @NotNull TargetObject targetObject,
            @NotNull Supplier<Object> constructor
    ) {
        return findPath(targetObject, constructor, UnaryOperator.identity());
    }

    private @NotNull Triple<Object, Node, PathNode> bfs(
            @NotNull Triple<Object, Node, PathNode> triple,
            @NotNull Node finalNode,
            @NotNull List<EdgeMethod> methods,
            @NotNull UnaryOperator<Object> copyObject,
            @NotNull UnaryOperator<Object> termination
    ) {
        Queue<Triple<Object, Node, PathNode>> queue = new ArrayDeque<>(Collections.singleton(triple));
        Set<Node> visited = new HashSet<>();

        while (!queue.isEmpty()) {
            triple = queue.poll();

            Node curNode = triple.getSecond();

            if (curNode.equals(finalNode)) {
                return triple;
            }
            if (visited.contains(curNode)) {
                continue;
            }
            visited.add(curNode);

            int lastDiff = finalNode.diff(triple.getSecond());
            List<Triple<Object, Node, PathNode>> lowerLevel = applyMethods(methods, triple, copyObject, termination);
            List<Integer> diffs = lowerLevel.stream().map(t -> finalNode.diff(t.getSecond())).collect(Collectors.toList());

            int minDif = diffs.stream().min(Integer::compareTo).orElse(Integer.MAX_VALUE);
            if (minDif >= lastDiff) {
                return triple;
            }
            queue = queue.stream().filter(t -> finalNode.diff(t.getSecond()) == minDif).collect(Collectors.toCollection(ArrayDeque::new));
            for (int i = 0; i < diffs.size(); i++) {
                Triple<Object, Node, PathNode> triple1 = lowerLevel.get(i);
                if (minDif == diffs.get(i) && !visited.contains(triple1.getSecond())) {
                    queue.add(triple1);
                }
            }
        }
        return triple;
    }

    List<Triple<Object, Node, PathNode>> applyMethods(
            @NotNull List<EdgeMethod> edgeMethods,
            @NotNull Triple<Object, Node, PathNode> triple,
            @NotNull UnaryOperator<Object> copyObject,
            @NotNull UnaryOperator<Object> termination
    ) {
        List<Triple<Object, Node, PathNode>> lowerLevel = new ArrayList<>();
        PathNode prevPath = triple.getThird();

        for (EdgeMethod edgeMethod : edgeMethods) {
            Object instance = copyObject.apply(triple.getFirst());
            try {
                edgeMethod.invoke(instance);
            } catch (Exception ignored) {
                logging(edgeMethod);
                continue;
            }
            Object instanceBuilt = Utils.callSupplierWrapper(() -> termination.apply(instance));
            lowerLevel.add(new Triple<>(instance, ClassFieldExtractor.extract(instanceBuilt), new PathNode(prevPath, edgeMethod)));
        }
        return lowerLevel;
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

    private static void logging(@NotNull EdgeMethod edgeMethod) {
        String funcName = edgeMethod.getMethod().getName();
        String argNames = Arrays.toString(edgeMethod.getArgs());
        String msg = String.format("Error calling the function \"%s\" with arguments %s", funcName, argNames);
        LOGGER.warning(msg);
    }
}
