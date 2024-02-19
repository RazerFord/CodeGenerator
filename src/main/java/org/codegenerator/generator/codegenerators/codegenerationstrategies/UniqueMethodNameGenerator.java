package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import kotlin.Pair;
import org.apache.commons.lang3.StringUtils;
import org.codegenerator.history.HistoryNode;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Deque;

public class UniqueMethodNameGenerator {
    private final TypeSpec.Builder typeBuilder;
    private final Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack;

    public UniqueMethodNameGenerator(
            TypeSpec.Builder typeBuilder,
            Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack
    ) {
        this.typeBuilder = typeBuilder;
        this.stack = stack;
    }

    public String generate(@NotNull Class<?> clazz) {
        String suffix = String.valueOf(typeBuilder.methodSpecs.size() + stack.size());
        String simpleName = clazz.getSimpleName();
        if (clazz.isArray()) {
            return String.format("createArray%s%s", StringUtils.capitalize(simpleName.replaceAll("(\\[])", "")), suffix);
        }
        return String.format("create%s%s", StringUtils.capitalize(simpleName), suffix);
    }
}
