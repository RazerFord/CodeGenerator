package org.codegenerator.generator.graph;

import com.rits.cloning.Cloner;
import kotlin.Triple;
import org.codegenerator.ClonerUtilities;
import org.codegenerator.CommonUtils;
import org.codegenerator.CustomLogger;
import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.codegenerator.generator.graph.edges.Edge;
import org.codegenerator.generator.graph.edges.EdgeGenerator;
import org.codegenerator.generator.graph.edges.EdgeMethod;
import org.codegenerator.generator.objectwrappers.Range;
import org.jacodb.api.JcField;
import org.jacodb.api.JcLookup;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class LazyMethodGraph {
    private static final Logger LOGGER = CustomLogger.getLogger();

    private final EdgeGenerator edgeGenerator = new EdgeGenerator();
    private final Cloner cloner = ClonerUtilities.standard();

    private Edge<?> termination = IDENTITY;

    public Edge<?> getTermination() {
        return termination;
    }

    public void setTermination(Edge<?> termination) {
        this.termination = termination;
    }

    public @NotNull Path findPath(@NotNull Range range) {
        return findPath(range, new ArrayList<>());
    }

    public @NotNull Path findPath(@NotNull Range range, List<Edge<? extends Executable>> path) {
        Object from = range.getFrom().getObject();
        Object to = range.getTo().getObject();

        Node toNode = ClassFieldExtractor.extract(to);
        Triple<Object, Node, PathNode> triple = createTriple(from, PathNode.START);

        List<EdgeMethod> methods = generateEdge(range);
        triple = bfs(triple, toNode, methods);

        return collectPath(triple, path, toNode);
    }

    private List<EdgeMethod> generateEdge(@NotNull Range range) {
        return edgeGenerator.generate(range.getFrom().getClazz().getMethods(), range.getTo().get());
    }

    @Contract("_, _, _ -> new")
    private static @NotNull Path collectPath(
            @NotNull Triple<Object, Node, PathNode> triple,
            List<Edge<? extends Executable>> edges,
            Node toNode
    ) {
        PathNode finalPathNode = triple.component3();
        Deque<EdgeMethod> path = new ArrayDeque<>();
        while (finalPathNode != null && finalPathNode.edgeMethod != null) {
            path.addFirst(finalPathNode.edgeMethod);
            finalPathNode = finalPathNode.prevPathNode;
        }

        edges.addAll(path);

        return new Path(triple.component1(), toNode.diff(triple.component2()), edges);
    }

    private @NotNull Triple<Object, Node, PathNode> bfs(
            @NotNull Triple<Object, Node, PathNode> triple,
            @NotNull Node toNode,
            @NotNull List<EdgeMethod> methods
    ) {
        Queue<Triple<Object, Node, PathNode>> queue = new ArrayDeque<>(Collections.singleton(triple));
        Set<Node> visited = new HashSet<>();
        int lastMinDiff = Integer.MAX_VALUE;

        while (!queue.isEmpty()) {
            triple = queue.poll();

            Node curNode = triple.component2();

            if (curNode.equals(toNode)) {
                return triple;
            }
            if (visited.contains(curNode)) {
                continue;
            }
            visited.add(curNode);

            List<Triple<Object, Node, PathNode>> lowerLevel = applyMethods(methods, triple);

            List<Integer> diffs = lowerLevel.stream().map(t -> toNode.diff(t.component2()))
                    .collect(Collectors.toList());

            int minDiff = Collections.min(diffs);
            if (minDiff >= lastMinDiff) {
                return triple;
            }
            lastMinDiff = minDiff;

            queue = queue.stream().filter(t -> toNode.diff(t.component2()) == minDiff)
                    .collect(Collectors.toCollection(ArrayDeque::new));

            for (int i = 0; i < diffs.size(); i++) {
                Triple<Object, Node, PathNode> triple1 = lowerLevel.get(i);
                if (minDiff == diffs.get(i) && !visited.contains(triple1.component2())) {
                    queue.add(triple1);
                }
            }
        }
        return triple;
    }

    List<Triple<Object, Node, PathNode>> applyMethods(
            @NotNull List<EdgeMethod> edgeMethods,
            @NotNull Triple<Object, Node, PathNode> triple
    ) {
        List<Triple<Object, Node, PathNode>> lowerLevel = new ArrayList<>();
        PathNode prevPath = triple.component3();

        for (EdgeMethod edgeMethod : edgeMethods) {
            Object instance = cloner.deepClone(triple.component1());
            try {
                edgeMethod.invoke(instance);
                lowerLevel.add(createTriple(instance, new PathNode(prevPath, edgeMethod)));
            } catch (Exception ignored) {
                LOGGER.warning(CommonUtils.message(edgeMethod));
            }
        }
        return lowerLevel;
    }

    private static final class PathNode {
        private static final PathNode START = new PathNode(null, null, 0);

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

    private static final Edge<UnaryOperator<Object>> IDENTITY = new Edge<UnaryOperator<Object>>() {
        private final UnaryOperator<Object> identity = UnaryOperator.identity();

        @Override
        public Object invoke(Object object) {
            return identity.apply(object);
        }

        @Override
        public Object invoke() {
            throw new UnsupportedOperationException();
        }

        @Override
        public UnaryOperator<Object> getMethod() {
            return identity;
        }

        @Contract(value = " -> new", pure = true)
        @Override
        public Object @NotNull [] getArgs() {
            return new Object[0];
        }

        @Override
        public JcMethod toJcMethod(@NotNull JcLookup<JcField, JcMethod> lookup) {
            throw new UnsupportedOperationException();
        }
    };

    @Contract("_, _ -> new")
    private @NotNull Triple<Object, Node, PathNode> createTriple(Object o, PathNode p) {
        return new Triple<>(o, ClassFieldExtractor.extract(termination.invoke(o)), p);
    }
}
