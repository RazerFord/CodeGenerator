package org.codegenerator.generator.graph.resultfinding;

import org.codegenerator.generator.objectwrappers.RangeResult;

import java.util.Collections;
import java.util.List;

public class RangeResultFindingImpl extends ResultFindingImpl implements RangeResultFinding {
    private final List<RangeResult> ranges;
    private final Class<?> creator;

    public RangeResultFindingImpl(
            Object targetObject,
            int deviation,
            Class<?> creator,
            List<Object> suspects,
            List<RangeResult> ranges
    ) {
        super(targetObject, deviation, suspects);
        this.creator = creator;
        this.ranges = ranges;
    }

    @Override
    public List<RangeResult> getRanges() {
        return Collections.unmodifiableList(ranges);
    }

    @Override
    public Class<?> getCreator() {
        return creator;
    }
}
