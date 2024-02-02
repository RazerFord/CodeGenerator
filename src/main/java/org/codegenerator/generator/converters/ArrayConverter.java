package org.codegenerator.generator.converters;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.lang3.StringUtils;
import org.codegenerator.generator.codegenerators.MethodCodeGenerator;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static org.codegenerator.Utils.throwIf;

public class ArrayConverter implements Converter {
    private final String variableName;
    private final String methodName;
    private final MethodCodeGenerator methodCodeGenerator;

    public ArrayConverter(String variableName,
                          String methodName,
                          @NotNull MethodCodeGenerator methodCodeGenerator) {
        this.variableName = variableName;
        this.methodName = methodName;
        this.methodCodeGenerator = methodCodeGenerator;
    }

    @Override
    public boolean canConvert(@NotNull Object o) {
        return o.getClass().isArray();
    }

    @Override
    public String convert(@NotNull Object o, TypeSpec.@NotNull Builder typeBuilder, MethodSpec.@NotNull Builder methodBuilder) {
        throwIf(!canConvert(o), new IllegalArgumentException());

        Class<?> clazz = o.getClass();
        Class<?> componentType = clazz.getComponentType();
        int deep = StringUtils.countMatches(componentType.getName(), "[");
        Class<?> typeArray = getArrayType(clazz);

        CodeBlock codeBlock = createCodeBlockOfCreatingVariable(clazz, typeArray, Array.getLength(o), deep);
        MethodSpec.Builder methodBuilder1 = MethodSpec.constructorBuilder().addStatement(codeBlock);
        for (int i = 0, length = Array.getLength(o); i < length; i++) {
            Object element = Array.get(o, i);
            String call = createCallRecursively(componentType, element, typeBuilder, methodBuilder1);
            methodBuilder1.addStatement("$L[$L] = $L", variableName, i, call);
        }
        methodBuilder1.addStatement("return $L", variableName);

        String newMethodName = createNewMethodName(clazz, typeBuilder.methodSpecs.size());
        methodBuilder1.setName(newMethodName).addModifiers(PUBLIC, STATIC).returns(clazz);
        typeBuilder.addMethod(methodBuilder1.build());

        return buildMethodCall(newMethodName);
    }

    private Class<?> getArrayType(Class<?> clazz) {
        Class<?> typeArray = null;
        for (; clazz != null; clazz = clazz.getComponentType()) {
            typeArray = clazz;
        }
        return typeArray;
    }

    private @NotNull CodeBlock createCodeBlockOfCreatingVariable(
            Class<?> typeVariable,
            Class<?> typeArray,
            int length,
            int deep
    ) {
        return CodeBlock.builder()
                .add(
                        "$T $L = new $T[$L]$L",
                        typeVariable,
                        variableName,
                        typeArray,
                        length,
                        StringUtils.repeat("[]", deep)
                )
                .build();
    }

    private String createCallRecursively(
            @NotNull Class<?> componentType,
            Object element,
            TypeSpec.Builder typeBuilder,
            MethodSpec.Builder methodBuilder
    ) {
        if (componentType.isArray()) {
            return convert(element, typeBuilder, methodBuilder);
        }
        String[] call = new String[1];
        List<Buildable> buildableList = Collections.singletonList(
                (converter, typeBuilder1, methodBuilder1) ->
                        call[0] = converter.convert(element, typeBuilder1, methodBuilder1)
        );
        methodCodeGenerator.generate(buildableList, typeBuilder, methodBuilder);
        return call[0];
    }

    private @NotNull String createNewMethodName(@NotNull Class<?> clazz, int number) {
        return methodName + StringUtils.capitalize(clazz.getSimpleName().replaceAll("(\\[])", "")) + number;
    }

    @Contract(pure = true)
    private @NotNull String buildMethodCall(String methodName) {
        return methodName + "()";
    }
}
