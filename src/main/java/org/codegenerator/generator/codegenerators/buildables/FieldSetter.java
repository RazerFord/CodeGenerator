package org.codegenerator.generator.codegenerators.buildables;

import com.squareup.javapoet.*;
import org.codegenerator.generator.converters.Converter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class FieldSetter implements Buildable {
    private final String fieldName;
    private final Class<?> ownerField;
    private final String mapName;
    private final String variableName;
    private final Object value;

    public FieldSetter(
            @NotNull Field field,
            String mapName,
            String variableName,
            Object value
    ) {
        this.fieldName = field.getName();
        this.ownerField = field.getDeclaringClass();
        this.mapName = mapName;
        this.variableName = variableName;
        this.value = value;
    }

    @Override
    public void build(
            @NotNull Converter converter,
            TypeSpec.@NotNull Builder typeBuilder,
            MethodSpec.@NotNull Builder methodBuilder
    ) {
        methodBuilder.addStatement("$L.get($T.class).get($S).set($L, $L)", mapName, ownerField, fieldName, variableName, converter.convert(value, typeBuilder, methodBuilder));
    }
}
