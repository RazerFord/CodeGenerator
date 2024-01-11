package org.codegenerator.generator.converters;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;

public class PrimitiveTypeArrayConverter {
    private final ConverterPrimitiveTypesAndString converterPrimitiveTypesAndString = new ConverterPrimitiveTypesAndString();

    public String convert(@NotNull Object o) {
        Class<?> clazz = o.getClass();
        if (!clazz.isArray()) {
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
