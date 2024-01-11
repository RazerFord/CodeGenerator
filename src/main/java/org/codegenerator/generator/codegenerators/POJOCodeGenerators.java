package org.codegenerator.generator.codegenerators;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.converters.Converter;
import org.codegenerator.generator.converters.ConverterPipeline;
import org.codegenerator.generator.converters.ConverterPrimitiveTypesAndString;
import org.codegenerator.generator.converters.PrimitiveTypeArrayConverter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.STATIC;

public class POJOCodeGenerators {
    private final Converter converter = new ConverterPipeline(Arrays.asList(new ConverterPrimitiveTypesAndString(), new PrimitiveTypeArrayConverter()));
    private final Class<?> clazz;
    private final String packageName;
    private final String className;
    private final String methodName;

    public POJOCodeGenerators(@NotNull Class<?> clazz, String packageName, String className, String methodName) {
        this.clazz = clazz;
        this.packageName = packageName;
        this.className = className;
        this.methodName = methodName;
    }

    public void generate(@NotNull List<MethodCall> methodCalls, Path path) {
        generateCode(methodCalls, path);
    }

    private @NotNull List<CodeBlock> generateCodeBlocks(@NotNull List<MethodCall> methodCalls, TypeSpec.Builder generatedClassBuilder) {
        List<CodeBlock> codeBlocks = new ArrayList<>();

        codeBlocks.add(CodeBlock.builder().add("$T object = new $T()", clazz, clazz).build());

        for (MethodCall methodCall : methodCalls) {
            codeBlocks.add(generateCodeBlock(methodCall, generatedClassBuilder));
        }
        codeBlocks.add(CodeBlock.builder().add("return object").build());
        return codeBlocks;
    }

    private @NotNull CodeBlock generateCodeBlock(@NotNull MethodCall methodCall, TypeSpec.Builder generatedClassBuilder) {
        Map<String, String> args = new HashMap<>();
        args.put(PREFIX_METHOD, methodCall.getMethod().getName());
        StringBuilder format = new StringBuilder("object.$func:L(");
        Object[] methodArgs = methodCall.getArgs();
        for (int i = 0; i < methodArgs.length; i++) {
            String argFormat = String.format("%s%s", PREFIX_ARG, i);
            args.put(argFormat, converter.convert(methodArgs[i]));
            format.append(String.format("$%s:L,", argFormat));
        }
        if (methodCall.getArgs().length > 0) {
            format.setCharAt(format.length() - 1, ')');
        }
        return CodeBlock.builder().addNamed(format.toString(), args).build();
    }

    private void generateCode(@NotNull List<MethodCall> methodCalls, Path path) {
        TypeSpec.Builder generatedClassBuilder = TypeSpec.classBuilder(className).addModifiers(PUBLIC, FINAL);

        List<CodeBlock> codeBlocks = generateCodeBlocks(methodCalls, generatedClassBuilder);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName).addModifiers(PUBLIC, STATIC).returns(clazz);

        codeBlocks.forEach(methodBuilder::addStatement);

        MethodSpec method = methodBuilder.build();

        JavaFile javaFile = JavaFile.builder(packageName, generatedClassBuilder.addMethod(method).build()).build();

        try {
            javaFile.writeTo(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String PREFIX_METHOD = "func";
    private static final String PREFIX_ARG = "arg";
}
