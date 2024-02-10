package org.codegenerator.generator.codegenerators.buildables;

import com.squareup.javapoet.*;
import org.codegenerator.generator.converters.Converter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Map;

public class CreationMapGetterField implements Buildable {
    private final String variableName;
    private final String mapName;
    private final String methodName;

    public CreationMapGetterField(
            String variableName,
            String mapName,
            String methodName
    ) {
        this.variableName = variableName;
        this.mapName = mapName;
        this.methodName = methodName;
    }

    @Override
    public void build(
            @NotNull Converter converter,
            TypeSpec.@NotNull Builder typeBuilder,
            MethodSpec.@NotNull Builder methodBuilder
    ) {
        ClassName mapClassName = ClassName.get(Map.class);
        ClassName className = ClassName.get(Class.class);
        TypeName wildcard = WildcardTypeName.subtypeOf(Object.class);
        ParameterizedTypeName classType = ParameterizedTypeName.get(className, wildcard);
        ParameterizedTypeName mapType = ParameterizedTypeName.get(Map.class, String.class, Field.class);
        ParameterizedTypeName mapMapType = ParameterizedTypeName.get(mapClassName, classType, mapType);

        methodBuilder.addStatement("$T $L = $L($L.getClass())", mapMapType, mapName, methodName, variableName);
    }
}
