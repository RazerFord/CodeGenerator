package org.codegenerator.generator.methodsequencefinders.concrete;

import org.codegenerator.generator.TargetObject;
import org.codegenerator.generator.graph.resultfinding.ResultFinding;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;

public interface MethodSequenceFinder {
    boolean canTry(TargetObject targetObject);

    ResultFinding findReflectionCallsInternal(@NotNull TargetObject targetObject, History<Executable> history);

    ResultFinding findJacoDBCallsInternal(@NotNull TargetObject targetObject, History<JcMethod> history);
}
