package org.codegenerator.generator.methodsequencefinders.internal;

import org.codegenerator.generator.TargetObject;
import org.codegenerator.generator.graph.resultfinding.ResultFinding;
import org.codegenerator.generator.graph.resultfinding.WrapperResultFinding;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryPrimitive;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Collections;

public class NullMethodSequenceFinder implements MethodSequenceFinderInternal {
    @Override
    public boolean canTry(@NotNull TargetObject targetObject) {
        return targetObject.getObject() == null;
    }

    @Override
    public ResultFinding findReflectionCallsInternal(@NotNull TargetObject targetObject, @NotNull History<Executable> history) {
        return findCallsInternal(targetObject.getObject(), history);
    }

    @Override
    public ResultFinding findJacoDBCallsInternal(@NotNull TargetObject targetObject, @NotNull History<JcMethod> history) {
        return findCallsInternal(targetObject.getObject(), history);
    }

    private <T> @NotNull WrapperResultFinding findCallsInternal(Object object, @NotNull History<T> history) {
        history.put(object, new HistoryPrimitive<>(object, Collections.emptyList(), NullMethodSequenceFinder.class));
        return WrapperResultFinding.withEmptySuspects();
    }
}
