package org.codegenerator.generator.codegenerators.codegenerationelements;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MethodBuilderWrapper {
    private final List<CodeBlockBuilderWrapper> blockBuilderWrappers = new ArrayList<>();
    private final MethodSpec defaultMethod;

    public MethodBuilderWrapper(MethodSpec.@NotNull Builder defaultMethodBuilder) {
        this.defaultMethod = defaultMethodBuilder.build();
    }

    public List<CodeBlockBuilderWrapper> getBlockBuilderWrappers() {
        return blockBuilderWrappers;
    }

    public MethodSpec getDefaultMethod() {
        return defaultMethod;
    }

    public MethodSpec build() {
        MethodSpec.Builder builder = copy(defaultMethod);
        blockBuilderWrappers.forEach(cb -> builder.addCode(cb.build()));
        return builder.build();
    }

    private static MethodSpec.@NotNull Builder copy(@NotNull MethodSpec method) {
        MethodSpec.Builder copy = MethodSpec.methodBuilder(method.name);
        copy.addJavadoc(copy(method.javadoc));
        copy.annotations.addAll(method.annotations);
        copy.modifiers.addAll(method.modifiers);
        copy.typeVariables.addAll(method.typeVariables);
        copy.returns(method.returnType);
        copy.parameters.addAll(method.parameters);
        copy.addExceptions(method.exceptions);
        copy.addCode(copy(method.code));
        copy.varargs(method.varargs);
        copy.defaultValue(copy(method.defaultValue));
        return copy;
    }

    @Contract("_ -> new")
    private static @NotNull CodeBlock copy(CodeBlock block) {
        return CodeBlock.builder()
                .add(block)
                .build();
    }
}
