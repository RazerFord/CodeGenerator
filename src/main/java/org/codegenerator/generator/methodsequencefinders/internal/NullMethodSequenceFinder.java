package org.codegenerator.generator.methodsequencefinders.internal;

import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.generator.methodsequencefinders.internal.resultfinding.ResultFinding;
import org.codegenerator.generator.methodsequencefinders.internal.resultfinding.WrapperResultFinding;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryObject;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Executable;
import java.util.Collections;
import java.util.List;

public class NullMethodSequenceFinder implements MethodSequenceFinderInternal {
    @Override
    public boolean canTry(Object object) {
        return object == null;
    }

    @Override
    public List<Buildable> findBuildableList(@Nullable Object object) {
        return Collections.emptyList();
    }

    @Override
    public ResultFinding findReflectionCallsInternal(@Nullable Object object, @NotNull History<Executable> history) {
        return findCallsInternal(object, history);
    }

    @Override
    public ResultFinding findJacoDBCallsInternal(@Nullable Object object, @NotNull History<JcMethod> history) {
        return findCallsInternal(object, history);
    }

    private <T> @NotNull WrapperResultFinding findCallsInternal(Object object, @NotNull History<T> history) {
        history.put(object, new HistoryObject<>(object, Collections.emptyList()));
        return WrapperResultFinding.withEmptySuspects();
    }
}
