package org.codegenerator.generator.codegenerators.buildables;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.converters.Converter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public final class ConstructorCall implements Buildable {
    private final Class<?> clazz;
    private final Constructor<?> constructor;
    private final Object[] args;

    public ConstructorCall(Class<?> clazz, Constructor<?> constructor, Object... args) {
        this.clazz = clazz;
        this.constructor = constructor;
        this.args = args;
    }

    @Override
    public void build(@NotNull Converter converter,
                      TypeSpec.@NotNull Builder typeBuilder,
                      MethodSpec.@NotNull Builder methodBuilder) {
        methodBuilder.addStatement(CodeBlock.builder().add("$T object = new $T()", clazz, clazz).build());
    }
}

