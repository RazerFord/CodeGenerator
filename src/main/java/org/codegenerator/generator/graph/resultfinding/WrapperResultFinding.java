package org.codegenerator.generator.graph.resultfinding;

import java.util.Collections;
import java.util.List;

public class WrapperResultFinding implements ResultFinding {
    public static final WrapperResultFinding WITH_EMPTY_SUSPECTS = new WrapperResultFinding(Collections.emptyList());
    private final List<Object> suspects;

    public WrapperResultFinding(List<Object> suspects) {
        this.suspects = suspects;
    }

    public Object getActualObject() {
        throw new UnsupportedOperationException("getActualObject");
    }

    public List<Object> getSuspects() {
        return suspects;
    }

    public boolean isSuccess() {
        return true;
    }

    public int getDeviation() {
        return 0;
    }

    public static WrapperResultFinding withEmptySuspects() {
        return WITH_EMPTY_SUSPECTS;
    }
}
