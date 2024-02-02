package org.codegenerator.generator.converters;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.jetbrains.annotations.NotNull;

public interface Converter {
    boolean canConvert(Object o);

    String convert(Object o, TypeSpec.@NotNull Builder generatedClassBuilder, MethodSpec.@NotNull Builder methodBuilder);
}
