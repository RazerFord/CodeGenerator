package org.codegenerator.generator.graph;

import org.codegenerator.generator.graph.edges.EdgeMethod;

import java.util.List;

public class Path {
    private final Object targetObject;
    private final int deviation;
    private final List<EdgeMethod> methods;

    public Path(
            Object targetObject,
            int deviation,
            List<EdgeMethod> methods
    ) {
        this.targetObject = targetObject;
        this.deviation = deviation;
        this.methods = methods;
    }

    public Object getTargetObject() {
        return targetObject;
    }

    public int getDeviation() {
        return deviation;
    }

    public List<EdgeMethod> getMethods() {
        return methods;
    }
}
