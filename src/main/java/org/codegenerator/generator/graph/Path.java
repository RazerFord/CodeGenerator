package org.codegenerator.generator.graph;

import org.codegenerator.generator.graph.edges.EdgeMethod;

import java.util.List;

public class Path {
    private final Object actualObject;
    private final int deviation;
    private final List<EdgeMethod> methods;

    public Path(
            Object actualObject,
            int deviation,
            List<EdgeMethod> methods
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

    public List<EdgeMethod> getMethods() {
        return methods;
    }
}
