package org.codegenerator.generator.converters;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.jetbrains.annotations.NotNull;

public class POJOObjectConverter implements Converter {
    private final String methodName;

    public POJOObjectConverter(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public boolean canConvert(Object o) {
        return true;
    }

    @Override
    public String convert(Object o, TypeSpec.@NotNull Builder generatedClassBuilder, MethodSpec.@NotNull Builder methodBuilder) {
        return null;
    }
}
