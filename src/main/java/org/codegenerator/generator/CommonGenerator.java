package org.codegenerator.generator;

import org.codegenerator.generator.methodsequencefinders.concrete.MethodSequenceFinder;

import java.util.Collection;
import java.util.function.Function;

/**
 * An interface for generating code that can be used to retrieve the requested object.
 * With this interface, you can make more flexible configuration for the code generator.
 */
public interface CommonGenerator extends Generator {
    /**
     * Register finders in the pipeline.
     *
     * @param methodSequenceFinderList collection of finders that will be added to Pipeline
     */
    void registerPipeline(Collection<Function<TargetObject, ? extends MethodSequenceFinder>>
                                  methodSequenceFinderList);

    /**
     * Register finder in the pipeline.
     *
     * @param methodSequenceFinder finder that will be added to Pipeline
     */
    void registerPipeline(Function<TargetObject, ? extends MethodSequenceFinder>
                                  methodSequenceFinder);

    /**
     * Clears the list of finders in Pipeline.
     */
    void unregisterPipeline();
}
