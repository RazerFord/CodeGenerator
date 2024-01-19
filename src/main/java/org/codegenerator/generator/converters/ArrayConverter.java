package org.codegenerator.generator.converters;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.codegenerators.MethodCodeGenerator;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static org.codegenerator.Utils.throwIf;

public class ArrayConverter implements Converter {
    private final String methodName;
    private final MethodCodeGenerator methodCodeGenerator;

    public ArrayConverter(String methodName, @NotNull MethodCodeGenerator methodCodeGenerator) {
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

        String newMethodName = methodName + componentType.getSimpleName() + typeBuilder.methodSpecs.size();
        ArrayTypeName arrayTypeName = ArrayTypeName.of(componentType);
        MethodSpec.Builder methodBuilder1 = MethodSpec.methodBuilder(newMethodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(arrayTypeName);

        methodBuilder1.addStatement("$1T[] array = new $1T[$2L]", componentType, Array.getLength(o));
        List<Buildable> buildableList = new ArrayList<>();
        int length = Array.getLength(o);
        for (int i = 0; i < length; i++) {
            Object o1 = Array.get(o, i);
            int i1 = i;
            buildableList.add((converter, typeBuilder1, methodBuilder2) ->
                    methodBuilder2.addStatement("array[$L] = $L", i1, converter.convert(o1, typeBuilder1, methodBuilder2)));
        }
        methodCodeGenerator.generate(buildableList, typeBuilder, methodBuilder1);

        methodBuilder1.addStatement("return array");
        typeBuilder.addMethod(methodBuilder1.build());

        return buildMethodCall(newMethodName);
    }

    @Contract(pure = true)
    private @NotNull String buildMethodCall(String methodName) {
        return methodName + "()";
    }
}
