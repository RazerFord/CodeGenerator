package org.codegenerator.generator.codegenerators.buildables;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.converters.Converter;
import org.jetbrains.annotations.NotNull;

public class CodeBlockBuildable implements Buildable {
    private final CodeBlock codeBlock;

    public CodeBlockBuildable(String code) {
        this(CodeBlock.builder().add(code).build());
    }

    public CodeBlockBuildable(CodeBlock codeBlock) {
        this.codeBlock = codeBlock;
    }

    @Override
    public void build(
            @NotNull Converter converter,
            TypeSpec.@NotNull Builder typeBuilder,
            MethodSpec.@NotNull Builder methodBuilder
    ) {
        methodBuilder.addCode(codeBlock);
    }

    public static @NotNull CodeBlockBuildable beginTryCatch() {
        CodeBlock codeBlock = CodeBlock.builder()
                .beginControlFlow("try")
                .build();

        return new CodeBlockBuildable(codeBlock);
    }

    public static @NotNull CodeBlockBuildable endTryCatch(Class<?> excpectionClass) {
        CodeBlock codeBlock = CodeBlock.builder()
                .nextControlFlow("catch ($T e) ", excpectionClass)
                .addStatement("throw new $T(e)", RuntimeException.class)
                .endControlFlow()
                .build();

        return new CodeBlockBuildable(codeBlock);
    }

    public static @NotNull CodeBlockBuildable createVariableBuilt(
            Class<?> newType,
            String newName,
            String name,
            String termination
    ) {
        CodeBlock codeBlock = CodeBlock.builder()
                .addStatement("$T $L = $L.$L()", newType, newName, name, termination)
                .build();

        return new CodeBlockBuildable(codeBlock);
    }
}
