package org.codegenerator.generator.methodsequencefinders.internal;

import org.codegenerator.generator.methodsequencefinders.internal.resultfinding.ResultFinding;
import org.codegenerator.generator.methodsequencefinders.internal.resultfinding.WrapperResultFinding;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryArray;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayMethodSequenceFinder implements MethodSequenceFinderInternal {
    @Override
    public boolean canTry(@NotNull Object object) {
        return object.getClass().isArray();
    }

    @Override
    public ResultFinding findReflectionCallsInternal(@NotNull Object object, @NotNull History<Executable> history) {
        return findCallsInternal(object, history);
    }

    @Override
    public ResultFinding findJacoDBCallsInternal(@NotNull Object object, @NotNull History<JcMethod> history) {
        return findCallsInternal(object, history);
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
            history.put(object, new HistoryArray<>(object, Collections.emptyList(), ArrayMethodSequenceFinder.class));
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++) {
                arrayTraversal(Array.get(object, i), suspects, history);
            }
        }
    }
}
