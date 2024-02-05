package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.List;

public interface MethodSequenceFinder {
    List<Buildable> findBuildableList(@NotNull Object finalObject);

    History<Executable> findReflectionCalls(@NotNull Object finalObject);

    History<JcMethod> findJacoDBCalls(@NotNull Object finalObject);
}
