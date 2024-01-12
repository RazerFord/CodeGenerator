package org.codegenerator.generator.codegenerators.buildables;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.converters.Converter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class ConstructorCall implements Buildable {
    private final Class<?> clazz;
    private final String variableName;
    private final Object[] args;

    public ConstructorCall(Class<?> clazz, String variableName, Object... args) {
        this.clazz = clazz;
        this.variableName = variableName;
        this.args = args;
    }

    @Override
    public void build(@NotNull Converter converter,
                      TypeSpec.@NotNull Builder typeBuilder,
                      MethodSpec.@NotNull Builder methodBuilder) {
        Map<String, String> argumentMap = new HashMap<>();
        StringBuilder format = new StringBuilder("(");
        Object[] methodArgs = args;
        for (int i = 0; i < methodArgs.length; i++) {
            String argFormat = String.format("%s%s", PREFIX_ARG, i);
            argumentMap.put(argFormat, converter.convert(methodArgs[i], typeBuilder, methodBuilder));
            format.append(String.format("$%s:L,", argFormat));
        }
        if (args.length > 0) {
            format.setCharAt(format.length() - 1, ')');
        } else {
            format.append(")");
        }
        CodeBlock codeBlock = CodeBlock.builder().addNamed(format.toString(), argumentMap).build();
        codeBlock = CodeBlock.builder().add("$T $L = new $T$L", clazz, variableName, clazz, codeBlock).build();
        methodBuilder.addStatement(codeBlock);
    }

    private static final String PREFIX_ARG = "arg";
}
