package org.codegenerator.generator.converters;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;

public class PrimitiveTypeArrayConverter implements Converter {
    private final ConverterPrimitiveTypesAndString converterPrimitiveTypesAndString = new ConverterPrimitiveTypesAndString();

    @Override
    public boolean canConvert(@NotNull Object o) {
        Class<?> clazz = o.getClass();
        return clazz.isArray();
    }

    @Override
    public String convert(@NotNull Object o) {
        Class<?> clazz = o.getClass();
        if (!canConvert(o)) {
            throw new IllegalArgumentException();
        }
        Class<?> componentType = clazz.getComponentType();
        StringBuilder stringBuilder = new StringBuilder(String.format("new %s[]{", componentType.getSimpleName()));
        int length = Array.getLength(o);
        for (int i = 0; i < length; i++) {
            String representationObject = converterPrimitiveTypesAndString.convert(Array.get(o, i));
            stringBuilder.append(representationObject).append(',');
        }
        if (length > 0) {
            stringBuilder.setCharAt(stringBuilder.length() - 1, '}');
        }
        return stringBuilder.toString();
    }
}
