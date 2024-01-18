package org.codegenerator.generator.codegenerators.buildables;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.converters.Converter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Collections;

public class MiddleChainingMethod implements Buildable {
    private final Method method;
    private final MethodCallCreator methodCallCreator;

    public MiddleChainingMethod(Method method, Object[] args) {
        this.method = method;
        methodCallCreator = new MethodCallCreator(args);
    }

    @Override
    public void build(@NotNull Converter converter,
                      TypeSpec.@NotNull Builder typeBuilder,
                      MethodSpec.@NotNull Builder methodBuilder) {
        CodeBlock codeBlock = CodeBlock.builder()
                .add(INDENT)
                .add(DOT)
                .add(method.getName())
                .add(methodCallCreator.build(converter, typeBuilder, methodBuilder))
                .add(NEW_LINE)
                .build();
        methodBuilder.addCode(codeBlock);
    }

    private static final String INDENT = String.join("", Collections.nCopies(8, " "));
    private static final String DOT = ".";
    private static final String NEW_LINE = "\n";
}
