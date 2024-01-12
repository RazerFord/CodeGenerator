package org.codegenerator.generator.codegenerators.buildables;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.converters.Converter;
import org.jetbrains.annotations.NotNull;

public interface Buildable {
    void build(@NotNull Converter converter, TypeSpec.@NotNull Builder typeBuilder, MethodSpec.@NotNull Builder methodBuilder);
}
