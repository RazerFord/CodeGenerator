package org.codegenerator.generator.methodsequencefinders.concrete;

import org.codegenerator.generator.objectwrappers.*;
import org.codegenerator.generator.graph.resultfinding.RangeResultFinding;
import org.codegenerator.generator.graph.resultfinding.RangeWrapperResultFinding;
import org.codegenerator.generator.graph.resultfinding.ResultFinding;
import org.codegenerator.generator.graph.resultfinding.WrapperResultFinding;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryNode;
import org.codegenerator.history.HistoryPrimitive;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Collections;
import java.util.List;

public class NullMethodSequenceFinder implements MethodSequenceFinder {

    @Override
    public HistoryNode<Executable> createNode(
            @NotNull TargetObject targetObject,
            List<HistoryCall<Executable>> calls,
            HistoryNode<Executable> next
    ) {
        return new HistoryPrimitive<>(targetObject.getObject(), NullMethodSequenceFinder.class);
    }

    @Override
    public boolean canTry(@NotNull TargetObject targetObject) {
        return targetObject.getObject() == null;
    }

    @Override
    public boolean canTry(@NotNull Range range) {
        return canTry(range.getFrom()) && canTry(range.getTo());
    }

    @Override
    public RangeResultFinding findRanges(Range range) {
        return new RangeWrapperResultFinding(new RangeResult(range), Collections.emptyList(), NullMethodSequenceFinder.class);
    }

    @Override
    public RangeResultFinding findRanges(TargetObject targetObject) {
        return findRanges(new FakeRange(targetObject));
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
        history.put(object, new HistoryPrimitive<>(object, NullMethodSequenceFinder.class));
        return WrapperResultFinding.withEmptySuspects();
    }
}
