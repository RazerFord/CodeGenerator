package org.codegenerator.generator.methodsequencefinders.concrete;

import org.codegenerator.generator.objectwrappers.Range;
import org.codegenerator.generator.objectwrappers.TargetObject;
import org.codegenerator.generator.graph.resultfinding.RangeResultFinding;
import org.codegenerator.generator.graph.resultfinding.ResultFinding;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryNode;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.List;

public interface MethodSequenceFinder {
    HistoryNode<Executable> createNode(@NotNull TargetObject targetObject, List<HistoryCall<Executable>> calls, HistoryNode<Executable> next);

    boolean canTry(TargetObject targetObject);

    boolean canTry(Range range);

    RangeResultFinding findRanges(Range range);

    RangeResultFinding findRanges(TargetObject targetObject);

    ResultFinding findReflectionCallsInternal(@NotNull TargetObject targetObject, History<Executable> history);

    ResultFinding findJacoDBCallsInternal(@NotNull TargetObject targetObject, History<JcMethod> history);
}
