package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.List;

public interface MethodSequenceFinderInternal extends MethodSequenceFinder {
    List<Object> findReflectionCallsInternal(@NotNull Object finalObject, History<Executable> history);

    List<Object> findJacoDBCallsInternal(@NotNull Object finalObject, History<JcMethod> history);
}
