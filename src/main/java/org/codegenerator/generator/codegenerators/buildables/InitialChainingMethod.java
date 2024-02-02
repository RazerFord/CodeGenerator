package org.codegenerator.generator.codegenerators.buildables;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.converters.Converter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public final class InitialChainingMethod implements Buildable {
    private final Method method;
    private final String variableName;
    private final CallCreator callCreator;

    public InitialChainingMethod(Method method, String variableName, Object... args) {
        this.method = method;
        this.variableName = variableName;
        callCreator = new CallCreator(args);
    }

    @Override
    public void build(@NotNull Converter converter,
                      TypeSpec.@NotNull Builder typeBuilder,
                      MethodSpec.@NotNull Builder methodBuilder) {
        CodeBlock codeBlock = CodeBlock.builder()
                .add(variableName)
                .add(DOT)
                .add(method.getName())
                .add(callCreator.build(converter, typeBuilder, methodBuilder))
                .add(NEW_LINE)
                .build();
        methodBuilder.addCode(codeBlock).build();
    }

    private static final String DOT = ".";
    private static final String NEW_LINE = "\n";
}
