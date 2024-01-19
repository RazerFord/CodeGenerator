package org.codegenerator.generator.converters;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.exceptions.FailedConvert;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.codegenerator.Utils.throwIf;

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
        throwIf(!canConvert(o), new IllegalArgumentException());
        for (Converter converter : converters) {
            try {
                if (converter.canConvert(o)) return converter.convert(o, generatedClassBuilder, methodBuilder);
            } catch (FailedConvert e) {
                throw e;
            } catch (Throwable ignored) {
            }
        }
        return null;
    }
}
