package org.codegenerator.generator.codegenerators.buildables;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.converters.Converter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class MethodCall implements Buildable {
    private final Method method;
    private final Object[] args;

    public MethodCall(Method method, Object... args) {
        this.method = method;
        this.args = args;
    }

    @Override
    public void build(@NotNull Converter converter,
                      TypeSpec.@NotNull Builder typeBuilder,
                      MethodSpec.@NotNull Builder methodBuilder) {
        Map<String, String> argumentMap = new HashMap<>();
        argumentMap.put(PREFIX_METHOD, method.getName());
        StringBuilder format = new StringBuilder("object.$func:L(");
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
        methodBuilder.addStatement(CodeBlock.builder().addNamed(format.toString(), argumentMap).build());
    }

    private static final String PREFIX_METHOD = "func";
    private static final String PREFIX_ARG = "arg";
}

