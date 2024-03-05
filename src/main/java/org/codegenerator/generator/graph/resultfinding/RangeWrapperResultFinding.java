package org.codegenerator.generator.graph.resultfinding;

import org.codegenerator.generator.objectwrappers.RangeResult;

import java.util.Collections;
import java.util.List;

public class RangeWrapperResultFinding extends WrapperResultFinding implements RangeResultFinding {
    private final RangeResult rangeObject;
    private final Class<?> creator;

    public RangeWrapperResultFinding(
            RangeResult rangeObject,
            List<Object> suspects,
            Class<?> creator
    ) {
        super(suspects);
        this.rangeObject = rangeObject;
        this.creator = creator;
    }

    @Override
    public List<RangeResult> getRanges() {
        return Collections.singletonList(rangeObject);
    }

    @Override
    public Class<?> getCreator() {
        return creator;
    }
}
