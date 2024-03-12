package org.codegenerator.generator.converters;

import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class PrimitiveConverter {
    private static final Map<Class<?>, Function<Object, String>> OBJECT_TO_STRING = createObjectToString();

    private PrimitiveConverter() {
    }

    public static boolean canConvert(@NotNull Object o) {
        Class<?> clazz = o.getClass();
        return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz == String.class;
    }

    public static String convert(@Nullable Object o) {
        Class<?> clazz = o != null ? o.getClass() : null;
        return OBJECT_TO_STRING.getOrDefault(clazz, Objects::toString).apply(o);
    }

    private static @NotNull Map<Class<?>, Function<Object, String>> createObjectToString() {
        Map<Class<?>, Function<Object, String>> objectToString = new HashMap<>();

        objectToString.put(byte.class, o -> String.format("(byte) %s", o));
        objectToString.put(Byte.class, o -> String.format("(byte) %s", o));

        objectToString.put(short.class, o -> String.format("(short) %s", o));
        objectToString.put(Short.class, o -> String.format("(short) %s", o));

        objectToString.put(long.class, o -> String.format("%sL", o));
        objectToString.put(Long.class, o -> String.format("%sL", o));

        objectToString.put(float.class, o -> String.format("%sF", o));
        objectToString.put(Float.class, o -> String.format("%sF", o));

        objectToString.put(char.class, o -> String.format("'%s'", o));
        objectToString.put(Character.class, o -> String.format("'%s'", o));

        objectToString.put(String.class, o -> String.format("\"%s\"", o));

        return objectToString;
    }
}
