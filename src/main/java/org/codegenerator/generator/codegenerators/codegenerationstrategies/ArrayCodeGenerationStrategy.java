package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import kotlin.Pair;
import org.apache.commons.lang3.StringUtils;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryNode;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Executable;
import java.util.Deque;

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
    public CodeGenerationStrategy generate(
            TypeSpec.@NotNull Builder typeBuilder,
            @NotNull Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack,
            History<Executable> history
    ) {
        Pair<HistoryNode<Executable>, MethodSpec.Builder> p = stack.pop();

        Object object = p.getFirst().getObject();
        MethodSpec.Builder methodBuilder = p.getSecond();

        addStatements(object, history, typeBuilder, methodBuilder, stack);

        typeBuilder.addMethod(methodBuilder.build());

        return new BeginCodeGenerationStrategy();
    }

    private void addStatements(
            @NotNull Object object,
            @NotNull History<Executable> history,
            TypeSpec.@NotNull Builder typeBuilder,
            MethodSpec.@NotNull Builder methodBuilder,
            @NotNull Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack
    ) {
        methodBuilder.addStatement(initArray(object));
        for (int i = 0, length = Array.getLength(object); i < length; i++) {
            UniqueMethodNameGenerator nameGenerator = new UniqueMethodNameGenerator(typeBuilder, stack);
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
