package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.*;
import kotlin.Pair;
import org.codegenerator.history.HistoryNode;
import org.codegenerator.history.SetterUsingReflection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class ReflectionCodeGeneration {
    private static final String METHOD_NAME = "getFields";
    private static final String MAP_NAME = "map";

    private final String methodName;
    private final String mapName;

    public ReflectionCodeGeneration() {
        this(METHOD_NAME, MAP_NAME);
    }

    public ReflectionCodeGeneration(String methodName, String mapName) {
        this.methodName = methodName;
        this.mapName = mapName;
    }

    public void generate(
            String variableName,
            TypeSpec.Builder typeBuilder,
            @NotNull Pair<HistoryNode<Executable>, MethodSpec.Builder> historyNodeBuilderPair,
            Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack
    ) {
        HistoryNode<Executable> historyNode = historyNodeBuilderPair.getFirst();
        MethodSpec.Builder methodBuilder = historyNodeBuilderPair.getSecond();

        if (historyNode.getSetterUsingReflections().isEmpty()) return;

        addMethodGetFieldsIfNonExists(typeBuilder);
        methodBuilder.addCode(createMap(variableName));
        methodBuilder.addCode(beginTryCatch());

        for (SetterUsingReflection<Executable> call : historyNode.getSetterUsingReflections()) {
            String suffix = String.valueOf(typeBuilder.methodSpecs.size() + stack.size());
            String value = Utils.toRepresentation(suffix, call.getHistoryArg(), stack);
            methodBuilder.addCode(setField(variableName, value, call.getField()));
        }
        methodBuilder.addCode(endTryCatch());
    }

    @Contract(" -> new")
    private static @NotNull CodeBlock beginTryCatch() {
        return CodeBlock.builder()
                .beginControlFlow("try")
                .build();
    }

    private static @NotNull CodeBlock endTryCatch() {
        return CodeBlock.builder()
                .nextControlFlow("catch ($T e) ", IllegalAccessException.class)
                .addStatement("throw new $T(e)", RuntimeException.class)
                .endControlFlow()
                .build();
    }

    private @NotNull CodeBlock createMap(String variableName) {
        ClassName mapClassName = ClassName.get(Map.class);
        ClassName className = ClassName.get(Class.class);
        TypeName wildcard = WildcardTypeName.subtypeOf(Object.class);
        ParameterizedTypeName classType = ParameterizedTypeName.get(className, wildcard);
        ParameterizedTypeName mapType = ParameterizedTypeName.get(Map.class, String.class, Field.class);
        ParameterizedTypeName mapMapType = ParameterizedTypeName.get(mapClassName, classType, mapType);

        return CodeBlock.builder()
                .addStatement("$T $L = $L($L.getClass())", mapMapType, mapName, methodName, variableName)
                .build();
    }

    private @NotNull CodeBlock setField(
            String variableName,
            String value,
            @NotNull Field field
    ) {
        return CodeBlock.builder()
                .addStatement("$L.get($T.class).get($S).set($L, $L)",
                        mapName, field.getDeclaringClass(), field.getName(), variableName, value)
                .build();
    }

    /**
     * Create and add a `getFields` method to `typeBuilder` if it doesn't exist
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
    private void addMethodGetFieldsIfNonExists(TypeSpec.@NotNull Builder typeBuilder) {
        ParameterSpec parameterSpec = ParameterSpec.builder(Class.class, "clazz").build();
        if (typeBuilder.methodSpecs.stream()
                .noneMatch(m -> m.name.equals(methodName) && m.parameters.equals(Collections.singletonList(parameterSpec)))) {
            ClassName mapClassName = ClassName.get(Map.class);
            ClassName classClassName = ClassName.get(Class.class);
            TypeName wildcard = WildcardTypeName.subtypeOf(Object.class);
            ParameterizedTypeName classType = ParameterizedTypeName.get(classClassName, wildcard);
            ParameterizedTypeName mapType = ParameterizedTypeName.get(Map.class, String.class, Field.class);
            ParameterizedTypeName mapMapType = ParameterizedTypeName.get(mapClassName, classType, mapType);

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
