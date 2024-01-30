package org.codegenerator.generator.converters;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static org.codegenerator.Utils.throwIf;

public class PrimitiveTypeArrayConverter implements Converter {
    private final ConverterPrimitiveTypesAndString converterPrimitiveTypesAndString = new ConverterPrimitiveTypesAndString();
    private final String variableName;
    private final String methodName;

    public PrimitiveTypeArrayConverter(String variableName, String methodName) {
        this.variableName = variableName;
        this.methodName = methodName;
    }

    @Override
    public boolean canConvert(@NotNull Object o) {
        return canConvert(o.getClass());
    }

    public boolean canConvert(@NotNull Class<?> clazz) {
        Class<?> componentType = clazz.getComponentType();
        return clazz.isArray() &&
                (componentType.isArray() ?
                        canConvert(componentType) :
                        (ClassUtils.isPrimitiveOrWrapper(componentType) || (String.class == componentType))
                );
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
            String call = createCallRecursively(componentType, element, typeBuilder, methodBuilder);
            methodBuilder1.addStatement("$L[$L] = $L", variableName, i, call);
        }
        methodBuilder1.addStatement("return $L", variableName);

        String newMethodName = createNewMethodName(clazz, typeBuilder.methodSpecs.size());
        methodBuilder1.setName(newMethodName).addModifiers(PUBLIC, STATIC).returns(clazz);
        typeBuilder.addMethod(methodBuilder1.build());

        return buildMethodCall(newMethodName);
    }

    private @NotNull CodeBlock createCodeBlockOfCreatingVariable(
            Class<?> typeVariable,
            Class<?> typeArray,
            int length,
            int deep
    ) {
        return CodeBlock.builder()
                .add(
                        "$T $L = new $L[$L]$L",
                        typeVariable,
                        variableName,
                        typeArray,
                        length,
                        StringUtils.repeat("[]", deep)
                )
                .build();
    }

    private Class<?> getArrayType(Class<?> clazz) {
        Class<?> typeArray = null;
        for (Class<?> cls = clazz; cls != null; cls = cls.getComponentType()) {
            typeArray = cls;
        }
        return typeArray;
    }

    private @NotNull String createNewMethodName(@NotNull Class<?> clazz, int number) {
        return methodName + StringUtils.capitalize(clazz.getSimpleName().replaceAll("(\\[])", "")) + number;
    }

    private String createCallRecursively(
            @NotNull Class<?> componentType,
            Object element,
            TypeSpec.Builder typeBuilder,
            MethodSpec.Builder methodBuilder
    ) {
        if (componentType.isArray()) return convert(element, typeBuilder, methodBuilder);
        else return converterPrimitiveTypesAndString.convert(element, typeBuilder, methodBuilder);
    }

    @Contract(pure = true)
    private @NotNull String buildMethodCall(String methodName) {
        return methodName + "()";
    }
}
