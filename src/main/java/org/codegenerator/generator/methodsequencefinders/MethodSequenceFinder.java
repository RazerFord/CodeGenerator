package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.generator.TargetObject;
import org.codegenerator.generator.methodsequencefinders.internal.MethodSequenceFinderInternal;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;

public interface MethodSequenceFinder {

    History<Executable> findReflectionCalls(@NotNull TargetObject targetObject);

    History<JcMethod> findJacoDBCalls(@NotNull TargetObject targetObject);

    void registerFinder(Class<?> clazz, MethodSequenceFinderInternal finder);
}
