package org.codegenerator.generator.converters;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;

import static org.codegenerator.Utils.throwIf;

public class PrimitiveTypeArrayConverter implements Converter {
    private final ConverterPrimitiveTypesAndString converterPrimitiveTypesAndString = new ConverterPrimitiveTypesAndString();

    @Override
    public boolean canConvert(@NotNull Object o) {
        Class<?> clazz = o.getClass();
        Class<?> componentType = clazz.getComponentType();
        return clazz.isArray() && (ClassUtils.isPrimitiveOrWrapper(componentType) || (String.class == componentType));
    }

    @Override
    public String convert(@NotNull Object o, TypeSpec.@NotNull Builder generatedClassBuilder, MethodSpec.@NotNull Builder methodBuilder) {
        throwIf(!canConvert(o), new IllegalArgumentException());
        Class<?> clazz = o.getClass();
        Class<?> componentType = clazz.getComponentType();
        StringBuilder stringBuilder = new StringBuilder(String.format("new %s[]{", componentType.getSimpleName()));
        int length = Array.getLength(o);
        for (int i = 0; i < length; i++) {
            String representationObject = converterPrimitiveTypesAndString.convert(Array.get(o, i), generatedClassBuilder, methodBuilder);
            stringBuilder.append(representationObject).append(',');
        }
        if (length > 0) {
            stringBuilder.setCharAt(stringBuilder.length() - 1, '}');
        }
        return stringBuilder.toString();
    }
}
