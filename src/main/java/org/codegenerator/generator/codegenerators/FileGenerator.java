package org.codegenerator.generator.codegenerators;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import kotlin.Pair;
import org.codegenerator.generator.codegenerators.codegenerationstrategies.BeginCodeGenerationStrategy;
import org.codegenerator.generator.codegenerators.codegenerationstrategies.CodeGenerationStrategy;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryNode;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;

import static javax.lang.model.element.Modifier.*;

public class FileGenerator {
    private CodeGenerationStrategy codeGenerationStrategy = new BeginCodeGenerationStrategy();

    public JavaFile generate(
            @NotNull History<Executable> history,
            @NotNull Object source,
            String packageName,
            String className,
            String methodName
    ) {
        TypeSpec.Builder typeBuilder = getTypeBuilder(className);
        @NotNull ContextGenerator context = buildContext(typeBuilder, source, methodName, history);
        Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack = context.getStack();

        while (!stack.isEmpty()) {
            codeGenerationStrategy = codeGenerationStrategy.generate(context);
        }

        return JavaFile.builder(packageName, typeBuilder.build()).build();
    }

    @NotNull ContextGenerator buildContext(
            TypeSpec.Builder typeBuilder,
            Object source,
            String methodName,
            @NotNull History<Executable> history
    ) {
        MethodSpec.Builder methodBuilder = getMethodBuilder(source, methodName);
        Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack = new ArrayDeque<>(Collections.singleton(new Pair<>(history.get(source), methodBuilder)));

        return ContextGenerator.builder()
                .setHistory(history)
                .setTypeBuilder(typeBuilder)
                .setStack(stack)
                .build();
    }

    @NotNull
    private MethodSpec.Builder getMethodBuilder(@NotNull Object source, String methodName) {
        return MethodSpec
                .methodBuilder(methodName)
                .addModifiers(PUBLIC, STATIC)
                .returns(source.getClass());
    }

    @NotNull
    private TypeSpec.Builder getTypeBuilder(String className) {
        return TypeSpec
                .classBuilder(className)
                .addModifiers(PUBLIC, FINAL);
    }
}
