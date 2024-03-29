package org.codegenerator.generator.codegenerators;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static javax.lang.model.element.Modifier.*;

public class ClassCodeGenerators {
    private final Class<?> clazz;
    private final MethodCodeGenerator methodCodeGenerator;

    public ClassCodeGenerators(@NotNull Class<?> clazz) {
        this.clazz = clazz;
        methodCodeGenerator = new MethodCodeGenerator();
    }

    public JavaFile generate(
            @NotNull List<Buildable> methodCalls,
            String packageName,
            String className,
            String methodName
    ) {
        TypeSpec.Builder generatedClassBuilder = TypeSpec.classBuilder(className).addModifiers(PUBLIC, FINAL);
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName).addModifiers(PUBLIC, STATIC).returns(clazz);

        methodCodeGenerator.generate(methodCalls, generatedClassBuilder, methodBuilder);

        MethodSpec method = methodBuilder.build();
        TypeSpec type = generatedClassBuilder.addMethod(method).build();
        return JavaFile.builder(packageName, type).build();
    }
}
