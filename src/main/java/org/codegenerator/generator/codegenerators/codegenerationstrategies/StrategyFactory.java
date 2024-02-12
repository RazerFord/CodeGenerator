package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import org.codegenerator.generator.methodsequencefinders.internal.BuilderMethodSequenceFinder;
import org.codegenerator.generator.methodsequencefinders.internal.POJOMethodSequenceFinder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class StrategyFactory {
    private StrategyFactory() {
    }

    private static final Map<Class<?>, Supplier<CodeGenerationStrategy>> CLASS_TO_STRATEGY = getClassToStrategy();

    @Contract("_ -> new")
    static @NotNull CodeGenerationStrategy getCodeGenerationStrategy(Class<?> clazz) {
        return CLASS_TO_STRATEGY.getOrDefault(clazz, () -> {
            throw new IllegalArgumentException();
        }).get();
    }

    private static @NotNull Map<Class<?>, Supplier<CodeGenerationStrategy>> getClassToStrategy() {
        Map<Class<?>, Supplier<CodeGenerationStrategy>> map = new HashMap<>();
        map.put(POJOMethodSequenceFinder.class, POJOCodeGenerationStrategy::new);
        map.put(BuilderMethodSequenceFinder.class, BuilderCodeGenerationStrategy::new);
        return map;
    }
}
