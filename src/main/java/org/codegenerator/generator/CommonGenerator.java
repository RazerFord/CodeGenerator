package org.codegenerator.generator;

import org.codegenerator.generator.methodsequencefinders.internal.MethodSequenceFinderInternal;

import java.util.Collection;
import java.util.function.Function;

public interface CommonGenerator extends Generator {
    void registerPipeline(Collection<Function<TargetObject, ? extends MethodSequenceFinderInternal>> methodSequenceFinderList);

    void registerPipeline(Function<TargetObject, ? extends MethodSequenceFinderInternal> methodSequenceFinder);

    void unregisterPipeline();
}
