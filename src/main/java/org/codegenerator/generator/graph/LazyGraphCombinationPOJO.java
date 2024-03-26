package org.codegenerator.generator.graph;

import org.codegenerator.generator.graph.edges.EdgeConstructor;
import org.codegenerator.generator.objectwrappers.Range;
import org.codegenerator.generator.objectwrappers.RangeObject;
import org.codegenerator.generator.objectwrappers.TargetObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;

public class LazyGraphCombinationPOJO {
    private final LazyConstructorGraph constructorGraph = new LazyConstructorGraph();
    private final LazyMethodGraph methodGraph = new LazyMethodGraph();

    public @NotNull Path findPath(@NotNull TargetObject targetObject) {
        EdgeConstructor edgeConstructor = constructorGraph.findPath(targetObject);
        return methodGraph.findPath(
                new RangeObject(new TargetObject(edgeConstructor.invoke()), targetObject),
                new ArrayList<>(Collections.singletonList(edgeConstructor))
        );
    }

    public @NotNull Path findPath(@NotNull Range range) {
        return methodGraph.findPath(range);
    }
}
