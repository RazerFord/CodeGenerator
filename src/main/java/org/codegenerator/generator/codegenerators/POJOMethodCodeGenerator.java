package org.codegenerator.generator.codegenerators;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.converters.Converter;
import org.codegenerator.generator.converters.ConverterPipeline;
import org.codegenerator.generator.converters.ConverterPrimitiveTypesAndString;
import org.codegenerator.generator.converters.PrimitiveTypeArrayConverter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class POJOMethodCodeGenerator {
    private final Converter converter = new ConverterPipeline(Arrays.asList(new ConverterPrimitiveTypesAndString(), new PrimitiveTypeArrayConverter()));
    private final Class<?> clazz;

    public POJOMethodCodeGenerator(@NotNull Class<?> clazz) {
        this.clazz = clazz;
    }

    public void generate(@NotNull List<MethodCall> methodCalls,
                         TypeSpec.@NotNull Builder typeBuilder,
                         MethodSpec.@NotNull Builder methodBuilder) {
        generateCodeBlocks(methodCalls, typeBuilder, methodBuilder);
    }

    private void generateCodeBlocks(@NotNull List<MethodCall> methodCalls,
                                    TypeSpec.Builder generatedClassBuilder,
                                    MethodSpec.@NotNull Builder methodBuilder) {
        methodBuilder.addStatement(CodeBlock.builder().add("$T object = new $T()", clazz, clazz).build());
        for (MethodCall methodCall : methodCalls) {
            generateCodeBlock(methodCall, generatedClassBuilder, methodBuilder);
        }
        methodBuilder.addStatement(CodeBlock.builder().add("return object").build());
    }

    private void generateCodeBlock(@NotNull MethodCall methodCall,
                                   TypeSpec.@NotNull Builder generatedClassBuilder,
                                   MethodSpec.@NotNull Builder methodBuilder) {
        Map<String, String> args = new HashMap<>();
        args.put(PREFIX_METHOD, methodCall.getMethod().getName());
        StringBuilder format = new StringBuilder("object.$func:L(");
        Object[] methodArgs = methodCall.getArgs();
        for (int i = 0; i < methodArgs.length; i++) {
            String argFormat = String.format("%s%s", PREFIX_ARG, i);
            args.put(argFormat, converter.convert(methodArgs[i], generatedClassBuilder, methodBuilder));
            format.append(String.format("$%s:L,", argFormat));
        }
        if (methodCall.getArgs().length > 0) {
            format.setCharAt(format.length() - 1, ')');
        }
        methodBuilder.addStatement(CodeBlock.builder().addNamed(format.toString(), args).build());
    }

    private static final String PREFIX_METHOD = "func";
    private static final String PREFIX_ARG = "arg";
}
