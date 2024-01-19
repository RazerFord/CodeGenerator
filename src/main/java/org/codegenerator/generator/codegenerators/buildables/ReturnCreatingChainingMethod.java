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

public class ReturnCreatingChainingMethod implements Buildable {
    private final Class<?> clazz;
    private final Executable executable;

    public ReturnCreatingChainingMethod(Class<?> clazz, Executable executable) {
        this.clazz = clazz;
        this.executable = executable;
    }

    @Override
    public void build(@NotNull Converter converter,
                      TypeSpec.@NotNull Builder typeBuilder,
                      MethodSpec.@NotNull Builder methodBuilder) {
        CodeBlock codeBlock = null;
        if (executable instanceof Constructor<?>) {
            codeBlock = CodeBlock.builder()
                    .add("return new $T()", clazz)
                    .add(NEW_LINE)
                    .build();
        }
        if (executable instanceof Method) {
            Method method = (Method) executable;
            Class<?> declaringClass = method.getDeclaringClass();
            codeBlock = CodeBlock.builder()
                    .add("return $T.$L()", declaringClass, method.getName())
                    .add(NEW_LINE)
                    .build();
        }
        methodBuilder.addCode(Objects.requireNonNull(codeBlock, ERROR_CONSTRUCT_CODE_BLOCK));
    }

    private static final String ERROR_CONSTRUCT_CODE_BLOCK = "The code block was not built";
    private static final String NEW_LINE = "\n";
}
