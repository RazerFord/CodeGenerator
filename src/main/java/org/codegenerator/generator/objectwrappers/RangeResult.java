package org.codegenerator.generator.objectwrappers;

import org.codegenerator.generator.graph.edges.Edge;

import java.lang.reflect.Executable;
import java.util.Collections;
import java.util.List;

public class RangeResult implements Range {
    private final Range range;
    private final List<Edge<? extends Executable>> methods;

    public RangeResult(Range range) {
        this(range, Collections.emptyList());
    }

    public RangeResult(Range range, List<Edge<? extends Executable>> methods) {
        this.range = range;
        this.methods = methods;
    }

    public TargetObject getFrom() {
        return range.getFrom();
    }

    public TargetObject getTo() {
        return range.getTo();
    }

    public List<Edge<? extends Executable>> getMethods() {
        return Collections.unmodifiableList(methods);
    }
}
