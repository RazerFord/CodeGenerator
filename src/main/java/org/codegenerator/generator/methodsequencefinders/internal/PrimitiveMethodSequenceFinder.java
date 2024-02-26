package org.codegenerator.generator.methodsequencefinders.internal;

import org.apache.commons.lang3.ClassUtils;
import org.codegenerator.generator.TargetObject;
import org.codegenerator.generator.graph.resultfinding.ResultFinding;
import org.codegenerator.generator.graph.resultfinding.WrapperResultFinding;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryPrimitive;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Collections;

public class PrimitiveMethodSequenceFinder implements MethodSequenceFinderInternal {
    @Override
    public boolean canTry(@NotNull TargetObject targetObject) {
        Class<?> clazz = targetObject.getClazz();
        return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz == String.class;
    }

    @Override
    public ResultFinding findReflectionCallsInternal(@NotNull TargetObject targetObject, @NotNull History<Executable> history) {
        return findCallsInternal(targetObject, history);
    }

    @Override
    public ResultFinding findJacoDBCallsInternal(@NotNull TargetObject targetObject, @NotNull History<JcMethod> history) {
        return findCallsInternal(targetObject, history);
    }

    private <T> ResultFinding findCallsInternal(@NotNull TargetObject targetObject, @NotNull History<T> history) {
        Object object = targetObject.getObject();
        history.put(object, new HistoryPrimitive<>(object, Collections.emptyList(), PrimitiveMethodSequenceFinder.class));
        return WrapperResultFinding.withEmptySuspects();
    }
}
