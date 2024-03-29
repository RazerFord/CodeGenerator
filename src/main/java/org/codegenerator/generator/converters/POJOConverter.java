package org.codegenerator.generator.converters;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.methodsequencefinders.internal.POJOMethodSequenceFinder;
import org.codegenerator.generator.codegenerators.MethodCodeGenerator;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class POJOConverter implements Converter {
    private final String methodName;
    private final MethodCodeGenerator methodCodeGenerator;

    public POJOConverter(String methodName, MethodCodeGenerator methodCodeGenerator) {
        this.methodName = methodName;
        this.methodCodeGenerator = methodCodeGenerator;
    }

    @Override
    public boolean canConvert(Object o) {
        return true;
    }

    @Override
    public String convert(@NotNull Object o, TypeSpec.@NotNull Builder typeBuilder, MethodSpec.@NotNull Builder methodBuilder) {
        Class<?> clazz = o.getClass();
        POJOMethodSequenceFinder pojoMethodSequenceFinder = new POJOMethodSequenceFinder();
        List<Buildable> pathNode = pojoMethodSequenceFinder.findBuildableList(o);
        MethodSpec.Builder methodBuilder1 = MethodSpec.constructorBuilder();
        methodCodeGenerator.generate(pathNode, typeBuilder, methodBuilder1);
        String newMethodName = methodName + clazz.getSimpleName() + typeBuilder.methodSpecs.size();
        methodBuilder1.setName(newMethodName).addModifiers(PUBLIC, STATIC).returns(clazz);
        typeBuilder.addMethod(methodBuilder1.build());
        return buildMethodCall(newMethodName);
    }

    @Contract(pure = true)
    private @NotNull String buildMethodCall(String methodName) {
        return methodName + "()";
    }
}
