package org.codegenerator.generator.objectwrappers;

public class FakeRange implements Range {
    private final TargetObject targetObject;

    public FakeRange(TargetObject targetObject) {
        this.targetObject = targetObject;
    }

    @Override
    public TargetObject getFrom() {
        return targetObject;
    }

    @Override
    public TargetObject getTo() {
        return targetObject;
    }
}
