package org.codegenerator.generator.graph;

import org.codegenerator.generator.graph.edges.Edge;

import java.lang.reflect.Executable;
import java.util.List;

public class Path {
    private final Object actualObject;
    private final int deviation;
    private final List<Edge<? extends Executable>> methods;

    public Path(
            Object actualObject,
            int deviation,
            List<Edge<? extends Executable>> methods
    ) {
        this.actualObject = actualObject;
        this.deviation = deviation;
        this.methods = methods;
    }

    public Object getActualObject() {
        return actualObject;
    }

    public int getDeviation() {
        return deviation;
    }

    public List<Edge<? extends Executable>> getMethods() {
        return methods;
    }
}
