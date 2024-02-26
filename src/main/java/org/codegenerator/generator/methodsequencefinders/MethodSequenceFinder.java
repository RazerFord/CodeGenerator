package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.generator.TargetObject;
import org.codegenerator.generator.methodsequencefinders.internal.MethodSequenceFinderInternal;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Collection;
import java.util.function.Function;

public interface MethodSequenceFinder {

    History<Executable> findReflectionCalls(@NotNull TargetObject targetObject);

    History<JcMethod> findJacoDBCalls(@NotNull TargetObject targetObject);

    void registerFinder(Class<?> clazz, MethodSequenceFinderInternal finder);

    void register(Collection<Function<TargetObject, ? extends MethodSequenceFinderInternal>> methodSequenceFinderList);

    void register(Function<TargetObject, ? extends MethodSequenceFinderInternal> methodSequenceFinder);

    void unregister();

    void reset();
}
