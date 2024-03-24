package org.codegenerator.generator.graph;

import org.codegenerator.generator.graph.edges.Edge;
import org.codegenerator.generator.graph.edges.EdgeConstructor;
import org.codegenerator.generator.objectwrappers.Range;
import org.codegenerator.generator.objectwrappers.TargetObject;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

public class LazyGraph {
    private final LazyConstructorGraph constructorGraph = new LazyConstructorGraph();
    private final LazyMethodGraph methodGraph = new LazyMethodGraph();

    public @NotNull Path findPath(@NotNull TargetObject targetObject, @NotNull UnaryOperator<Object> termination) {
        EdgeConstructor edgeConstructor = constructorGraph.findPath(targetObject);
        List<Edge<? extends Executable>> methods = new ArrayList<>(Collections.singletonList(edgeConstructor));
        return methodGraph.findPath(targetObject, edgeConstructor.invoke(), termination, methods);
    }

    public @NotNull Path findPath(@NotNull TargetObject targetObject) {
        return findPath(targetObject, UnaryOperator.identity());
    }

    public @NotNull Path findPath(@NotNull Range range) {
        return methodGraph.findPath(range.getTo(), range.getFrom().getObject(), UnaryOperator.identity());
    }
}
