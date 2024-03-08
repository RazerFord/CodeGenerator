package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import org.codegenerator.generator.codegenerators.MethodContext;
import org.codegenerator.generator.methodsequencefinders.concrete.BuilderMethodSequenceFinder;
import org.codegenerator.generator.methodsequencefinders.concrete.POJOMethodSequenceFinder;
import org.codegenerator.history.HistoryNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class StrategyFactory {
    private StrategyFactory() {
    }

    private static final Map<Class<?>, Supplier<CodeGenerationStrategy>> CLASS_TO_STRATEGY = getClassToStrategy();
    private static final Map<Class<?>, Supplier<CodeGenerationStrategy>> CLASS_TO_ADDED_STRATEGY = getClassToAddedStrategy();
    private static final Map<Class<?>, Supplier<CodeGenerationStrategy>> CLASS_TO_GENERIC_STRATEGY = getClassToGenericStrategy();

    @Contract("_ -> new")
    static @NotNull CodeGenerationStrategy getCodeGenerationStrategy(@NotNull MethodContext<Executable> methodContext) {
        HistoryNode<Executable> node = methodContext.getNode();
        if (methodContext.isAdded()) {
            return CLASS_TO_ADDED_STRATEGY.getOrDefault(node.getCreatorType(), () -> {
                throw new IllegalArgumentException();
            }).get();
        } else {
            Map<Class<?>, Supplier<CodeGenerationStrategy>> map = node.isGeneric() ? CLASS_TO_GENERIC_STRATEGY : CLASS_TO_STRATEGY;

            return map.getOrDefault(node.getCreatorType(), () -> {
                throw new IllegalArgumentException();
            }).get();
        }
    }

    private static @NotNull Map<Class<?>, Supplier<CodeGenerationStrategy>> getClassToStrategy() {
        Map<Class<?>, Supplier<CodeGenerationStrategy>> map = new HashMap<>();
        map.put(POJOMethodSequenceFinder.class, POJOCodeGenerationStrategy::new);
        map.put(BuilderMethodSequenceFinder.class, BuilderCodeGenerationStrategy::new);
        return map;
    }

    private static @NotNull Map<Class<?>, Supplier<CodeGenerationStrategy>> getClassToAddedStrategy() {
        Map<Class<?>, Supplier<CodeGenerationStrategy>> map = new HashMap<>();
        map.put(POJOMethodSequenceFinder.class, POJOAddedCodeGenerationStrategy::new);
        return map;
    }

    private static @NotNull Map<Class<?>, Supplier<CodeGenerationStrategy>> getClassToGenericStrategy() {
        Map<Class<?>, Supplier<CodeGenerationStrategy>> map = new HashMap<>();
        map.put(POJOMethodSequenceFinder.class, POJOGenericCodeGenerationStrategy::new);
        map.put(BuilderMethodSequenceFinder.class, BuilderGenericCodeGenerationStrategy::new);
        return map;
    }
}
