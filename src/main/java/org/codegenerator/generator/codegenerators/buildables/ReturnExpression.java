package org.codegenerator.generator.codegenerators.buildables;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.converters.Converter;
import org.jetbrains.annotations.NotNull;

public final class ReturnExpression implements Buildable {
    private final String variableName;

    public ReturnExpression(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public void build(@NotNull Converter converter,
                      TypeSpec.@NotNull Builder typeBuilder,
                      MethodSpec.@NotNull Builder methodBuilder) {
        methodBuilder.addStatement(CodeBlock.builder().add("return $L", variableName).build());
    }
}
