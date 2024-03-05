package org.codegenerator.generator.objectwrappers;

public class RangeObject implements Range {
    private final TargetObject from;
    private final TargetObject to;

    public RangeObject(Object from, Object to) {
        this(new TargetObject(from), new TargetObject(to));
    }

    public RangeObject(TargetObject from, TargetObject to) {
        this.from = from;
        this.to = to;
    }

    public TargetObject getFrom() {
        return from;
    }

    public TargetObject getTo() {
        return to;
    }
}
