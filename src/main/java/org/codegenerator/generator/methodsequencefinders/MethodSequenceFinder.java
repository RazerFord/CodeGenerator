package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.Call;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.List;

public interface MethodSequenceFinder {
    List<Buildable> findBuildableList(@NotNull Object finalObject);

    List<Call<Executable>> findReflectionCalls(@NotNull Object finalObject);

    List<Call<JcMethod>> findJacoDBCalls(@NotNull Object finalObject);
}
