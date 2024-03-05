package org.codegenerator.generator;

import org.codegenerator.generator.methodsequencefinders.MethodSequencePipeline;
import org.codegenerator.generator.methodsequencefinders.concrete.MethodSequenceFinder;
import org.codegenerator.generator.objectwrappers.TargetObject;

import java.util.Collection;
import java.util.function.Function;

/**
 * An interface for generating code that can be used to retrieve the requested object.
 * With this interface, you can make a more flexible configuration for the code generator.
 * To learn more about how method search works, check out the {@link MethodSequencePipeline} comments.
 *
 * @see MethodSequencePipeline
 */
public interface CommonGenerator extends Generator {
    /**
     * Register finders in the pipeline.
     *
     * @param methodSequenceFinderList collection of finders that will be added to the Pipeline
     */
    void registerPipeline(Collection<Function<TargetObject, ? extends MethodSequenceFinder>>
                                  methodSequenceFinderList);

    /**
     * Register finder in the pipeline.
     *
     * @param methodSequenceFinder finder that will be added to the Pipeline
     */
    void registerPipeline(Function<TargetObject, ? extends MethodSequenceFinder>
                                  methodSequenceFinder);

    /**
     * Clears the list of finders in the Pipeline.
     */
    void unregisterPipeline();
}
