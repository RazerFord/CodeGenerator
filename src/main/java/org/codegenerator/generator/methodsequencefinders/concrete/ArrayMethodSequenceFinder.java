package org.codegenerator.generator.methodsequencefinders.concrete;

import org.codegenerator.generator.graph.resultfinding.RangeResultFinding;
import org.codegenerator.generator.graph.resultfinding.RangeWrapperResultFinding;
import org.codegenerator.generator.graph.resultfinding.ResultFinding;
import org.codegenerator.generator.graph.resultfinding.WrapperResultFinding;
import org.codegenerator.generator.objectwrappers.FakeRange;
import org.codegenerator.generator.objectwrappers.Range;
import org.codegenerator.generator.objectwrappers.RangeResult;
import org.codegenerator.generator.objectwrappers.TargetObject;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryArray;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryNode;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.List;

public class ArrayMethodSequenceFinder implements MethodSequenceFinder {

    @Override
    public HistoryNode<Executable> createNode(
            @NotNull TargetObject targetObject,
            List<HistoryCall<Executable>> calls,
            HistoryNode<Executable> next
    ) {
        return new HistoryArray<>(targetObject.getObject(), ArrayMethodSequenceFinder.class);
    }

    @Override
    public boolean canTry(@NotNull TargetObject targetObject) {
        return targetObject.getClazz().isArray();
    }

    @Override
    public boolean canTry(@NotNull Range range) {
        return canTry(range.getFrom()) && range.getFrom().getObject() == range.getTo().getObject();
    }

    @Override
    public RangeResultFinding findRanges(@NotNull Range range) {
        List<Object> suspects = new ArrayList<>();
        arrayTraversal(range.getFrom().getObject(), suspects);
        return new RangeWrapperResultFinding(new RangeResult(range), suspects, ArrayMethodSequenceFinder.class);
    }

    @Override
    public RangeResultFinding findRanges(@NotNull TargetObject targetObject) {
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

    private <T> @NotNull ResultFinding findCallsInternal(@NotNull Object object, @NotNull History<T> history) {
        List<Object> suspects = new ArrayList<>();
        arrayTraversal(object, suspects, history);
        return new WrapperResultFinding(suspects);
    }

    private static <T> void arrayTraversal(Object object, List<Object> suspects, History<T> history) {
        if (object == null || !object.getClass().isArray()) {
            suspects.add(object);
        } else {
            history.put(object, new HistoryArray<>(object, ArrayMethodSequenceFinder.class));
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++) {
                arrayTraversal(Array.get(object, i), suspects, history);
            }
        }
    }

    private static void arrayTraversal(Object object, @NotNull List<Object> suspects) {
        suspects.add(object);
        if (object != null && object.getClass().isArray()) {
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++) {
                arrayTraversal(Array.get(object, i), suspects);
            }
        }
    }
}
