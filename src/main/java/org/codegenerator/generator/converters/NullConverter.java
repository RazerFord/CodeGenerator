package org.codegenerator.generator.converters;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.jetbrains.annotations.NotNull;

public class NullConverter implements Converter {
    @Override
    public boolean canConvert(Object o) {
        return o == null;
    }

    @Override
    public String convert(Object o, TypeSpec.@NotNull Builder generatedClassBuilder, MethodSpec.@NotNull Builder methodBuilder) {
        return "null";
    }
}
