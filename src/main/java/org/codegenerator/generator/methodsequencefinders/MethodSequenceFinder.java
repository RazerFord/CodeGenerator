package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.generator.methodsequencefinders.internal.MethodSequenceFinderInternal;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;

public interface MethodSequenceFinder {

    History<Executable> findReflectionCalls(@NotNull Object object);

    History<JcMethod> findJacoDBCalls(@NotNull Object object);

    void registerFinder(Class<?> clazz, MethodSequenceFinderInternal finder);
}
