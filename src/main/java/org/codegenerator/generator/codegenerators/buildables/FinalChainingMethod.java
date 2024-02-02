package org.codegenerator.generator.codegenerators.buildables;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.converters.Converter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Collections;

public class FinalChainingMethod implements Buildable {
    private final Method method;
    private final CallCreator callCreator;

    public FinalChainingMethod(Method method, Object... args) {
        this.method = method;
        callCreator = new CallCreator(args);
    }

    @Override
    public void build(@NotNull Converter converter,
                      TypeSpec.@NotNull Builder typeBuilder,
                      MethodSpec.@NotNull Builder methodBuilder) {
        CodeBlock codeBlock = CodeBlock.builder()
                .add(INDENT)
                .add(DOT)
                .add(method.getName())
                .add(callCreator.build(converter, typeBuilder, methodBuilder))
                .build();
        methodBuilder.addStatement(codeBlock);
    }

    private static final String INDENT = String.join("", Collections.nCopies(8, " "));
    private static final String DOT = ".";
}
