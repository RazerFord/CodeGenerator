package org.codegenerator.generator.codegenerators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.generator.converters.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class MethodCodeGenerator {
    private final Converter converter = createPipeline();
    private final Class<?> clazz;

    public MethodCodeGenerator(@NotNull Class<?> clazz) {
        this.clazz = clazz;
    }

    public void generate(@NotNull List<Buildable> methodCalls,
                         TypeSpec.@NotNull Builder typeBuilder,
                         MethodSpec.@NotNull Builder methodBuilder) {
        for (Buildable methodCall : methodCalls) {
            methodCall.build(converter, typeBuilder, methodBuilder);
        }
    }

    @Contract(" -> new")
    private static @NotNull Converter createPipeline() {
        List<Converter> converters = Arrays.asList(
                new ConverterPrimitiveTypesAndString(),
                new PrimitiveTypeArrayConverter(),
                new POJOObjectConverter(METHOD_NAME),
                new FailedConverter()
        );
        return new ConverterPipeline(converters);
    }

    private static final String METHOD_NAME = "createPojo";
}
