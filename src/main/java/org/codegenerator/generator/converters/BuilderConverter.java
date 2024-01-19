package org.codegenerator.generator.converters;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.codegenerators.MethodCodeGenerator;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.generator.methodsequencefinders.BuilderMethodSequenceFinder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import java.util.List;

public class BuilderConverter implements Converter {
    private final String methodName;
    private final MethodCodeGenerator methodCodeGenerator;

    public BuilderConverter(String methodName, MethodCodeGenerator methodCodeGenerator) {
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
        BuilderMethodSequenceFinder builderMethodSequenceFinder = new BuilderMethodSequenceFinder(clazz);
        List<Buildable> buildableList = builderMethodSequenceFinder.find(o);
        MethodSpec.Builder methodBuilder1 = MethodSpec.constructorBuilder();
        methodCodeGenerator.generate(buildableList, typeBuilder, methodBuilder1);
        String newMethodName = methodName + clazz.getSimpleName() + typeBuilder.methodSpecs.size();
        methodBuilder1.setName(newMethodName).addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(clazz);
        typeBuilder.addMethod(methodBuilder1.build());
        return buildMethodCall(newMethodName);
    }

    @Contract(pure = true)
    private @NotNull String buildMethodCall(String methodName) {
        return methodName + "()";
    }
}