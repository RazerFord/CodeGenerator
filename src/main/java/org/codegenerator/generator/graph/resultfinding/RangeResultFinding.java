package org.codegenerator.generator.graph.resultfinding;

import org.codegenerator.generator.objectwrappers.RangeResult;

import java.util.List;

public interface RangeResultFinding {
    List<RangeResult> getRanges();

    Class<?> getCreator();
}
