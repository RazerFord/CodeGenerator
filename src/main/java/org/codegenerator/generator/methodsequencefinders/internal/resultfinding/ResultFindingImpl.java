package org.codegenerator.generator.methodsequencefinders.internal.resultfinding;

import java.util.List;

public class ResultFindingImpl implements ResultFinding {
    private final Object targetObject;
    private final int deviation;
    private final List<Object> suspects;

    public ResultFindingImpl(
            Object targetObject,
            int deviation,
            List<Object> suspects
    ) {
        this.targetObject = targetObject;
        this.deviation = deviation;
        this.suspects = suspects;
    }

    @Override
    public Object getTargetObject() {
        return targetObject;
    }

    @Override
    public List<Object> getSuspects() {
        return suspects;
    }

    @Override
    public boolean isSuccess() {
        return deviation == 0;
    }

    @Override
    public int getDeviation() {
        return deviation;
    }
}
