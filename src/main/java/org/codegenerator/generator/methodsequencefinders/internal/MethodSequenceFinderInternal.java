package org.codegenerator.generator.methodsequencefinders.internal;

import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.generator.methodsequencefinders.internal.resultfinding.ResultFinding;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.List;

public interface MethodSequenceFinderInternal {
    boolean canTry(Object object);

    List<Buildable> findBuildableList(@NotNull Object object);

    ResultFinding findReflectionCallsInternal(@NotNull Object finalObject, History<Executable> history);

    ResultFinding findJacoDBCallsInternal(@NotNull Object finalObject, History<JcMethod> history);
}
