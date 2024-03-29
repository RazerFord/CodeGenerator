package org.codegenerator.generator.converters;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.codegenerator.Utils.throwIf;

public class ConverterPrimitiveTypesAndString implements Converter {
    public static final ConverterPrimitiveTypesAndString INSTANCE = new ConverterPrimitiveTypesAndString();
    private final Map<Class<?>, Function<Object, String>> converter = new HashMap<>();

    public ConverterPrimitiveTypesAndString() {
        converter.put(byte.class, o -> String.format("(byte) %s", o));
        converter.put(Byte.class, o -> String.format("(byte) %s", o));

        converter.put(short.class, o -> String.format("(short) %s", o));
        converter.put(Short.class, o -> String.format("(short) %s", o));

        converter.put(long.class, o -> String.format("%sL", o));
        converter.put(Long.class, o -> String.format("%sL", o));

        converter.put(float.class, o -> String.format("%sF", o));
        converter.put(Float.class, o -> String.format("%sF", o));

        converter.put(char.class, o -> String.format("'%s'", o));
        converter.put(Character.class, o -> String.format("'%s'", o));

        converter.put(String.class, o -> String.format("\"%s\"", o));
    }

    @Override
    public boolean canConvert(@NotNull Object o) {
        Class<?> clazz = o.getClass();
        return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz == String.class;
    }

    @Override
    public String convert(@NotNull Object o, TypeSpec.@NotNull Builder ignored0, MethodSpec.@NotNull Builder ignored1) {
        Class<?> clazz = o.getClass();
        throwIf(!canConvert(o), new IllegalArgumentException());
        return converter.getOrDefault(clazz, Object::toString).apply(o);
    }
    public static String convert(@NotNull Object o) {
        Class<?> clazz = o.getClass();
        return INSTANCE.converter.getOrDefault(clazz, Object::toString).apply(o);
    }
}
