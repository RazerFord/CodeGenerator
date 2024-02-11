package org.codegenerator.generator.methodsequencefinders.internal;

import org.apache.commons.lang3.ClassUtils;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.generator.methodsequencefinders.internal.resultfinding.ResultFinding;
import org.codegenerator.generator.methodsequencefinders.internal.resultfinding.WrapperResultFinding;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryPrimitive;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Collections;
import java.util.List;

public class PrimitiveMethodSequenceFinder implements MethodSequenceFinderInternal {
    @Override
    public boolean canTry(@NotNull Object object) {
        Class<?> clazz = object.getClass();
        return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz == String.class;
    }

    @Override
    public List<Buildable> findBuildableList(@NotNull Object object) {
        return Collections.emptyList();
    }

    @Override
    public ResultFinding findReflectionCallsInternal(@NotNull Object object, @NotNull History<Executable> history) {
        history.put(object, new HistoryPrimitive<>(object, Collections.emptyList(), PrimitiveMethodSequenceFinder.class));
        return WrapperResultFinding.withEmptySuspects();
    }

    @Override
    public ResultFinding findJacoDBCallsInternal(@NotNull Object object, @NotNull History<JcMethod> history) {
        history.put(object, new HistoryPrimitive<>(object, Collections.emptyList(), PrimitiveMethodSequenceFinder.class));
        return WrapperResultFinding.withEmptySuspects();
    }
}
