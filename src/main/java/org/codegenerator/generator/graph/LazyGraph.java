package org.codegenerator.generator.graph;

import org.codegenerator.generator.graph.edges.EdgeConstructor;
import org.codegenerator.generator.graph.path.ModifiablePath;
import org.codegenerator.generator.objectwrappers.TargetObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public class LazyGraph {
    private final LazyConstructorGraph constructorGraph;
    private final LazyMethodGraph methodGraph;

    public LazyGraph(LazyConstructorGraph constructorGraph, LazyMethodGraph methodGraph) {
        this.constructorGraph = constructorGraph;
        this.methodGraph = methodGraph;
    }

    public @NotNull ModifiablePath findPath(
            @NotNull TargetObject targetObject,
            @NotNull UnaryOperator<Object> termination
    ) {
        EdgeConstructor edgeConstructor = constructorGraph.findPath(targetObject);
        ModifiablePath path = methodGraph.findPath(targetObject, edgeConstructor.invoke(), termination);
        return new ModifiablePath(path.getActualObject(), path.getDeviation(), path.getMethods());
    }
}
