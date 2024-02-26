package org.codegenerator.generator.methodsequencefinders.internal;

import org.codegenerator.generator.TargetObject;
import org.codegenerator.generator.graph.resultfinding.ResultFinding;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;

public interface MethodSequenceFinderInternal {
    boolean canTry(TargetObject targetObject);

    ResultFinding findReflectionCallsInternal(@NotNull TargetObject targetObject, History<Executable> history);

    ResultFinding findJacoDBCallsInternal(@NotNull TargetObject targetObject, History<JcMethod> history);
}
