package org.codegenerator.generator.codegenerators.buildables;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.converters.Converter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Objects;

public class BeginChainingMethod implements Buildable {
    private final Class<?> clazz;
    private final String variableName;
    private final Executable executable;

    public BeginChainingMethod(Class<?> clazz, String variableName, Executable executable) {
        this.clazz = clazz;
        this.variableName = variableName;
        this.executable = executable;
    }

    @Override
    public void build(@NotNull Converter converter,
                      TypeSpec.@NotNull Builder typeBuilder,
                      MethodSpec.@NotNull Builder methodBuilder) {
        CodeBlock codeBlock = null;
        if (executable instanceof Constructor<?>) {
            codeBlock = CodeBlock.builder().add("$T $L = new $T()", clazz, variableName, clazz).build();
        }
        if (executable instanceof Method) {
            Method method = (Method) executable;
            Class<?> declaringClass = method.getDeclaringClass();
            codeBlock = CodeBlock.builder().add("$T $L = $T.$L()", clazz, variableName, declaringClass, method.getName()).build();
        }
        methodBuilder.addStatement(Objects.requireNonNull(codeBlock, ERROR_CONSTRUCT_CODE_BLOCK));
    }

    private static final String ERROR_CONSTRUCT_CODE_BLOCK = "The code block was not built";
}
