package org.codegenerator.generator.codegenerators;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.codegenerators.codegenerationelements.GenericResolver;
import org.codegenerator.generator.codegenerators.codegenerationstrategies.BeginCodeGenerationStrategy;
import org.codegenerator.generator.codegenerators.codegenerationstrategies.CodeGenerationStrategy;
import org.codegenerator.history.History;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Executable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;

import static javax.lang.model.element.Modifier.*;

/**
 * With this class, you can generate code that you can use to create the requested object.
 */
public class FileGenerator {
    private CodeGenerationStrategy codeGenerationStrategy = new BeginCodeGenerationStrategy();

    /**
     * Generates code that can be used to create the requested object.
     *
     * @param history     {@link History<Executable>} of the object
     * @param source      requested object
     * @param packageName the name of the package in which the generated code will be located
     * @param className   generated Class Name
     * @param methodName  the name of the method that will need to be called to retrieve the
     *                    requested object
     * @return file with the generated code
     */
    public @NotNull JavaFile generate(
            @NotNull History<Executable> history,
            @NotNull Object source,
            String packageName,
            String className,
            String methodName
    ) {
        TypeSpec.Builder typeBuilder = getTypeBuilder(className);
        @NotNull ContextGenerator context = buildContext(typeBuilder, source, methodName, history);
        Deque<MethodContext<Executable>> stack = context.getStack();

        while (!stack.isEmpty()) {
            codeGenerationStrategy = codeGenerationStrategy.generate(context);
        }

        context.getMethods().forEach(m -> typeBuilder.addMethod(m.build()));
        return JavaFile.builder(packageName, typeBuilder.build()).build();
    }

    private @NotNull ContextGenerator buildContext(
            TypeSpec.Builder typeBuilder,
            Object source,
            String methodName,
            @NotNull History<Executable> history
    ) {
        MethodSpec.Builder methodBuilder = getMethodBuilder(source, methodName);
        Deque<MethodContext<Executable>> stack = new ArrayDeque<>(Collections.singleton(new MethodContext<>(methodBuilder, history.get(source))));

        return ContextGenerator.builder()
                .setTypeBuilder(typeBuilder)
                .setMethods(new ArrayList<>())
                .setGenericResolver(new GenericResolver(history))
                .setStack(stack)
                .setHistory(history)
                .build();
    }

    private @NotNull MethodSpec.Builder getMethodBuilder(@Nullable Object source, String methodName) {
        return MethodSpec
                .methodBuilder(methodName)
                .addModifiers(PUBLIC, STATIC)
                .returns(source != null ? source.getClass() : Object.class);
    }

    private @NotNull TypeSpec.Builder getTypeBuilder(String className) {
        return TypeSpec
                .classBuilder(className)
                .addModifiers(PUBLIC, FINAL);
    }
}
