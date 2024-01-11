package org.codegenerator.generator.converters;

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
    public String convert(Object o) {
        if (!canConvert(o)) {
            throw new IllegalArgumentException();
        }
        for (Converter converter : converters) {
            if (converter.canConvert(o)) return converter.convert(o);
        }
        return null;
    }
}
