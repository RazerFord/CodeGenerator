package org.codegenerator.generator.methodsequencefinders.internal.resultfinding;

import java.util.List;

public interface ResultFinding {
    Object getTargetObject();

    List<Object> getSuspects();

    boolean isSuccess();

    int getDeviation();
}
