package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import org.apache.commons.lang3.StringUtils;
import org.codegenerator.generator.codegenerators.ContextGenerator;
import org.codegenerator.generator.codegenerators.MethodContext;
import org.codegenerator.history.History;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.util.Deque;
import java.util.List;

public class ArrayCodeGenerationStrategy implements CodeGenerationStrategy {
    private static final String VARIABLE_NAME = "object";

    private final String variableName;

    public ArrayCodeGenerationStrategy() {
        this(VARIABLE_NAME);
    }

    public ArrayCodeGenerationStrategy(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public CodeGenerationStrategy generate(@NotNull ContextGenerator context) {
        return generate(context.getMethods(), context.getStack(), context.getHistory());
    }

    private @NotNull CodeGenerationStrategy generate(
            @NotNull List<MethodSpec.Builder> methods,
            @NotNull Deque<MethodContext<Executable>> stack,
            History<Executable> history
    ) {
        MethodContext<Executable> p = stack.pop();

        Object object = p.getNode().getObject();
        MethodSpec.Builder methodBuilder = p.getMethod();

        addStatements(object, history, methods, methodBuilder, stack);

        methods.add(methodBuilder);

        return new BeginCodeGenerationStrategy();
    }

    private void addStatements(
            @NotNull Object object,
            @NotNull History<Executable> history,
            @NotNull List<MethodSpec.Builder> methods,
            MethodSpec.@NotNull Builder methodBuilder,
            @NotNull Deque<MethodContext<Executable>> stack
    ) {
        methodBuilder.addStatement(initArray(object));
        for (int i = 0, length = Array.getLength(object); i < length; i++) {
            UniqueMethodNameGenerator nameGenerator = new UniqueMethodNameGenerator(methods, stack);
            String call = Utils.toRepresentation(nameGenerator, history.get(Array.get(object, i)), stack);
            methodBuilder.addStatement("$L[$L] = $L", variableName, i, call);
        }
        methodBuilder.addStatement("return $L", variableName);
    }

    private Class<?> getArrayType(Class<?> clazz) {
        Class<?> typeArray = null;
        for (; clazz != null; clazz = clazz.getComponentType()) {
            typeArray = clazz;
        }
        return typeArray;
    }

    private @NotNull CodeBlock initArray(@NotNull Object object) {
        Class<?> typeObject = object.getClass();
        Class<?> componentType = typeObject.getComponentType();

        int deep = StringUtils.countMatches(componentType.getName(), "[");
        int length = Array.getLength(object);
        Class<?> typeArray = getArrayType(typeObject);

        return CodeBlock.builder()
                .add("$T $L = new $T[$L]$L", typeObject, variableName, typeArray, length, StringUtils.repeat("[]", deep))
                .build();
    }
}
