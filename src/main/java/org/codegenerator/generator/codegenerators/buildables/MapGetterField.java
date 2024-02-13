package org.codegenerator.generator.codegenerators.buildables;

import com.squareup.javapoet.*;
import org.codegenerator.generator.converters.Converter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class MapGetterField implements Buildable {
    private final String methodName;

    public MapGetterField(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Create and add a `getFields` method to `typeBuilder`
     * <pre>
     * {@code
     *  Map<Class < ?>, Map<String, Field>> getFields(Class<?> clazz) {
     *      Map<Class<?>, Map<String, Field>> classMapMap = new HashMap<>();
     *      for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
     *          Map<String, Field> fieldMap = new HashMap<>();
     *          for (Field field : clazz.getDeclaredFields()) {
     *              field.setAccessible(true);
     *              fieldMap.put(field.getName(), field);
     *          }
     *          classMapMap.put(clazz, fieldMap);
     *      }
     *      return classMapMap;
     *  }
     * }
     * </pre>
     */
    @Override
    public void build(
            @NotNull Converter converter,
            TypeSpec.@NotNull Builder typeBuilder,
            MethodSpec.@NotNull Builder methodBuilder
    ) {
        buildIfNonExists(typeBuilder);
    }

    private void buildIfNonExists(TypeSpec.@NotNull Builder typeBuilder) {
        ParameterSpec parameterSpec = ParameterSpec.builder(Class.class, "clazz").build();
        if (typeBuilder.methodSpecs.stream()
                .noneMatch(m -> m.name.equals(methodName) && m.parameters.equals(Collections.singletonList(parameterSpec)))) {
            ClassName mapName = ClassName.get(Map.class);
            ClassName className = ClassName.get(Class.class);
            TypeName wildcard = WildcardTypeName.subtypeOf(Object.class);
            ParameterizedTypeName classType = ParameterizedTypeName.get(className, wildcard);
            ParameterizedTypeName mapType = ParameterizedTypeName.get(Map.class, String.class, Field.class);
            ParameterizedTypeName mapMapType = ParameterizedTypeName.get(mapName, classType, mapType);

            MethodSpec methodSpec = MethodSpec.methodBuilder(methodName)
                    .addModifiers(PUBLIC, STATIC).returns(mapMapType)
                    .addParameter(parameterSpec)
                    .addStatement("$T classMapMap = new $T<>()", mapMapType, HashMap.class)
                    .beginControlFlow("for (; clazz != Object.class; clazz = clazz.getSuperclass())")
                    .addStatement("$T fieldMap = new $T<>()", mapType, HashMap.class)
                    .beginControlFlow("for (Field field : clazz.getDeclaredFields())")
                    .addStatement("field.setAccessible(true)")
                    .addStatement("fieldMap.put(field.getName(), field)")
                    .endControlFlow()
                    .addStatement("classMapMap.put(clazz, fieldMap)")
                    .endControlFlow()
                    .addStatement("return classMapMap")
                    .build();

            typeBuilder.addMethod(methodSpec);
        }
    }
}
