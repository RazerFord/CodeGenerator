package org.codegenerator.generator.codegenerators.buildables;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.converters.Converter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public final class MethodCall implements Buildable {
    private final Method method;
    private final CallCreator callCreator;

    public MethodCall(Method method, Object... args) {
        this.method = method;
        callCreator = new CallCreator(args);
    }

    @Override
    public void build(@NotNull Converter converter,
                      TypeSpec.@NotNull Builder typeBuilder,
                      MethodSpec.@NotNull Builder methodBuilder) {
        CodeBlock codeBlock = CodeBlock.builder()
                .add(PREFIX_METHOD_CALL)
                .add(method.getName())
                .add(callCreator.build(converter, typeBuilder, methodBuilder))
                .build();
        methodBuilder.addStatement(codeBlock).build();
    }

    private static final String PREFIX_METHOD_CALL = "object.";
}
