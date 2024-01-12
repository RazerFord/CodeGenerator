package org.codegenerator.generator.converters;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.codegenerators.MethodCall;
import org.codegenerator.generator.codegenerators.POJOGraphPathSearch;
import org.codegenerator.generator.codegenerators.POJOMethodCodeGenerator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class POJOObjectConverter implements Converter {
    private final String methodName;
    private int methodNumber = 0;

    public POJOObjectConverter(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public boolean canConvert(Object o) {
        return true;
    }

    @Override
    public String convert(@NotNull Object o, TypeSpec.@NotNull Builder generatedClassBuilder, MethodSpec.@NotNull Builder methodBuilder) {
        Class<?> clazz = o.getClass();
        POJOGraphPathSearch pojoGraphPathSearch = new POJOGraphPathSearch(clazz);
        List<MethodCall> pathNode = pojoGraphPathSearch.find(o);
        POJOMethodCodeGenerator pojoMethodCodeGenerator = new POJOMethodCodeGenerator(clazz);
        String newMethodName = methodName + methodNumber++;
        MethodSpec.Builder methodBuilder1 = MethodSpec.methodBuilder(newMethodName).addModifiers(PUBLIC, STATIC).returns(clazz);
        pojoMethodCodeGenerator.generate(pathNode, generatedClassBuilder, methodBuilder1);
        generatedClassBuilder.addMethod(methodBuilder1.build());
        return buildMethodCall(newMethodName);
    }

    @Contract(pure = true)
    private @NotNull String buildMethodCall(String methodName) {
        return methodName + "()";
    }
}
