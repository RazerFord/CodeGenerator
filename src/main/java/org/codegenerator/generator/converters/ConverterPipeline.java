package org.codegenerator.generator.converters;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConverterPipeline implements Converter {
    private final List<Converter> converters;

    public ConverterPipeline(List<Converter> converters) {
        this.converters = converters;
    }

    @Override
    public boolean canConvert(Object o) {
        for (Converter converter : converters) {
            if (converter.canConvert(o)) return true;
        }
        return false;
    }

    @Override
    public String convert(Object o, TypeSpec.@NotNull Builder generatedClassBuilder, MethodSpec.@NotNull Builder methodBuilder) {
        if (!canConvert(o)) {
            throw new IllegalArgumentException();
        }
        for (Converter converter : converters) {
            if (converter.canConvert(o)) return converter.convert(o, generatedClassBuilder, methodBuilder);
        }
        return null;
    }
}
