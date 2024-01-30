package org.codegenerator.generator.codegenerators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.generator.converters.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class MethodCodeGenerator {
    private final Converter converter = createPipeline(this);

    public void generate(
            @NotNull List<Buildable> methodCalls,
            TypeSpec.@NotNull Builder typeBuilder,
            MethodSpec.@NotNull Builder methodBuilder
    ) {
        for (Buildable methodCall : methodCalls) {
            methodCall.build(converter, typeBuilder, methodBuilder);
        }
    }

    private static @NotNull Converter createPipeline(MethodCodeGenerator methodCodeGenerator) {
        List<Converter> converters = Arrays.asList(
                new NullConverter(),
                new ConverterPrimitiveTypesAndString(),
                new PrimitiveTypeArrayConverter(ARRAY_VARIABLE_NAME, METHOD_NAME_ARRAY),
                new ArrayConverter(METHOD_NAME_ARRAY, methodCodeGenerator),
                new POJOConverter(METHOD_NAME_POJO, methodCodeGenerator),
                new BuilderConverter(METHOD_NAME_BUILDER, methodCodeGenerator),
                new FailedConverter()
        );
        return new ConverterPipeline(converters);
    }

    private static final String ARRAY_VARIABLE_NAME = "array";
    private static final String METHOD_NAME_ARRAY = "createArray";
    private static final String METHOD_NAME_POJO = "createPojo";
    private static final String METHOD_NAME_BUILDER = "createBuilder";
}
