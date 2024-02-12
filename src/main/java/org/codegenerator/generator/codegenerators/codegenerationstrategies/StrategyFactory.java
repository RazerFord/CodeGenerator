package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import org.codegenerator.generator.methodsequencefinders.internal.POJOMethodSequenceFinder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class StrategyFactory {
    private StrategyFactory() {
    }

    @Contract("_ -> new")
    static @NotNull CodeGenerationStrategy getCodeGenerationStrategy(Class<?> clazz) {
        if (clazz == POJOMethodSequenceFinder.class) {
            return new POJOCodeGenerationStrategy();
        } else {
            throw new IllegalArgumentException();
        }
    }
}
