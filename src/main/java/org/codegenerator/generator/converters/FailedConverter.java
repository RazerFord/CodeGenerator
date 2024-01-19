package org.codegenerator.generator.converters;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.exceptions.FailedConvert;
import org.jetbrains.annotations.NotNull;

public class FailedConverter implements Converter {
    @Override
    public boolean canConvert(Object o) {
        return true;
    }

    @Override
    public String convert(Object o, TypeSpec.@NotNull Builder generatedClassBuilder, MethodSpec.@NotNull Builder methodBuilder) {
        throw new FailedConvert();
    }
}
