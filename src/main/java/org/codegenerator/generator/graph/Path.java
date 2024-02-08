package org.codegenerator.generator.graph;

import org.codegenerator.generator.graph.edges.EdgeMethod;

import java.util.List;

public class Path {
    private final int deviation;
    private final List<EdgeMethod> methods;

    public Path(int deviation, List<EdgeMethod> methods) {
        this.deviation = deviation;
        this.methods = methods;
    }

    public int getDeviation() {
        return deviation;
    }

    public List<EdgeMethod> getMethods() {
        return methods;
    }
}
