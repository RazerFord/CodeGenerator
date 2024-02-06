package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.List;

public interface MethodSequenceFinder {
    List<Buildable> findBuildableList(@NotNull Object object);

    History<Executable> findReflectionCalls(@NotNull Object object);

    History<JcMethod> findJacoDBCalls(@NotNull Object object);
}
