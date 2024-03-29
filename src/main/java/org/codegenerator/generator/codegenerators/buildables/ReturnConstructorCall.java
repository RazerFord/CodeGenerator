package org.codegenerator.generator.codegenerators.buildables;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.converters.Converter;
import org.jetbrains.annotations.NotNull;

public final class ReturnConstructorCall implements Buildable {
    private final CallCreator callCreator;
    private final Class<?> clazz;

    public ReturnConstructorCall(Class<?> clazz, Object... args) {
        this.clazz = clazz;
        callCreator = new CallCreator(args);
    }

    @Override
    public void build(
            @NotNull Converter converter,
            TypeSpec.@NotNull Builder typeBuilder,
            MethodSpec.@NotNull Builder methodBuilder
    ) {
        CodeBlock codeBlock = callCreator.build(converter, typeBuilder, methodBuilder);
        codeBlock = CodeBlock.builder().add("return new $T$L", clazz, codeBlock).build();
        methodBuilder.addStatement(codeBlock);
    }
}
