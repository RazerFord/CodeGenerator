package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import org.apache.commons.lang3.StringUtils;
import org.codegenerator.generator.codegenerators.MethodContext;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Deque;
import java.util.List;

public class UniqueMethodNameGenerator {
    private final List<?> methods;
    private final Deque<MethodContext<Executable>> stack;

    public UniqueMethodNameGenerator(
            List<?> methods,
            Deque<MethodContext<Executable>> stack
    ) {
        this.methods = methods;
        this.stack = stack;
    }

    public String generate(@NotNull Class<?> clazz) {
        String suffix = String.valueOf(methods.size() + stack.size());
        String simpleName = clazz.getSimpleName();
        if (clazz.isArray()) {
            return String.format("createArray%s%s", StringUtils.capitalize(simpleName.replaceAll("(\\[])", "")), suffix);
        }
        return String.format("create%s%s", StringUtils.capitalize(simpleName), suffix);
    }
}
