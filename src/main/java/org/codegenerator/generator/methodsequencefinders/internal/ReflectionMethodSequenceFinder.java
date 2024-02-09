package org.codegenerator.generator.methodsequencefinders.internal;

import org.apache.commons.lang3.NotImplementedException;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.generator.methodsequencefinders.internal.resultfinding.ResultFinding;
import org.codegenerator.history.History;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Collections;
import java.util.List;

public class ReflectionMethodSequenceFinder implements MethodSequenceFinderInternal {

    @Override
    public boolean canTry(Object object) {
        return true;
    }

    @Override
    public List<Buildable> findBuildableList(@NotNull Object object) {
        return Collections.emptyList();
    }

    @Override
    public ResultFinding findReflectionCallsInternal(@NotNull Object finalObject, History<Executable> history) {
        throw new NotImplementedException();
    }

    @Override
    public ResultFinding findJacoDBCallsInternal(@NotNull Object finalObject, History<JcMethod> history) {
        throw new NotImplementedException();
    }
}
