package org.codegenerator.generator.methodsequencefinders.concrete;

import org.apache.commons.lang3.ClassUtils;
import org.codegenerator.generator.objectwrappers.FakeRange;
import org.codegenerator.generator.objectwrappers.Range;
import org.codegenerator.generator.objectwrappers.RangeResult;
import org.codegenerator.generator.objectwrappers.TargetObject;
import org.codegenerator.generator.graph.resultfinding.RangeResultFinding;
import org.codegenerator.generator.graph.resultfinding.RangeWrapperResultFinding;
import org.codegenerator.generator.graph.resultfinding.ResultFinding;
import org.codegenerator.generator.graph.resultfinding.WrapperResultFinding;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryPrimitive;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Collections;

public class PrimitiveMethodSequenceFinder implements MethodSequenceFinder {
    @Override
    public boolean canTry(@NotNull TargetObject targetObject) {
        Class<?> clazz = targetObject.getClazz();
        return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz == String.class;
    }

    @Override
    public boolean canTry(@NotNull Range range) {
        return canTry(range.getFrom()) && range.getFrom() == range.getTo();
    }

    @Override
    public RangeResultFinding findRanges(Range range) {
        return new RangeWrapperResultFinding(new RangeResult(range), Collections.emptyList(), PrimitiveMethodSequenceFinder.class);
    }

    @Override
    public RangeResultFinding findRanges(TargetObject targetObject) {
        return findRanges(new FakeRange(targetObject));
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
