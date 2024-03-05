package org.codegenerator.generator.graph.resultfinding;

import java.util.List;

public interface ResultFinding {
    Object getActualObject();

    List<Object> getSuspects();

    boolean isSuccess();

    int getDeviation();
}
