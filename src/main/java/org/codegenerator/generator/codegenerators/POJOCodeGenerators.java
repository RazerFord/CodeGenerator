package org.codegenerator.generator.codegenerators;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static javax.lang.model.element.Modifier.*;

public class POJOCodeGenerators {
    private final Class<?> clazz;
    private final String packageName;
    private final String className;
    private final String methodName;
    private final POJOMethodCodeGenerator pojoMethodCodeGenerator;

    public POJOCodeGenerators(@NotNull Class<?> clazz, String packageName, String className, String methodName) {
        this.clazz = clazz;
        this.packageName = packageName;
        this.className = className;
        this.methodName = methodName;
        pojoMethodCodeGenerator = new POJOMethodCodeGenerator(clazz);
    }

    public void generate(@NotNull List<MethodCall> methodCalls, Path path) {
        generateCode(methodCalls, path);
    }

    private void generateCode(@NotNull List<MethodCall> methodCalls, Path path) {
        TypeSpec.Builder generatedClassBuilder = TypeSpec.classBuilder(className).addModifiers(PUBLIC, FINAL);
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName).addModifiers(PUBLIC, STATIC).returns(clazz);

        pojoMethodCodeGenerator.generate(methodCalls, generatedClassBuilder, methodBuilder);

        MethodSpec method = methodBuilder.build();
        TypeSpec type = generatedClassBuilder.addMethod(method).build();
        JavaFile javaFile = JavaFile.builder(packageName, type).build();

        try {
            javaFile.writeTo(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
