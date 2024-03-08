package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.MethodSpec;
import org.codegenerator.generator.codegenerators.ContextGenerator;
import org.codegenerator.generator.codegenerators.MethodContext;
import org.codegenerator.generator.converters.PrimitiveConverter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Deque;
import java.util.List;

public class PrimitiveCodeGenerationStrategy implements CodeGenerationStrategy {
    @Override
    public CodeGenerationStrategy generate(@NotNull ContextGenerator context) {
        return generate(context.getMethods(), context.getStack());
    }

    private @NotNull CodeGenerationStrategy generate(
            @NotNull List<MethodSpec.Builder> methods,
            @NotNull Deque<MethodContext<Executable>> stack
    ) {
        MethodContext<Executable> p = stack.pop();
        Object object = p.getNode().getObject();
        MethodSpec.Builder methodBuilder = p.getMethod();

        methodBuilder.addStatement("return $L", PrimitiveConverter.convert(object));
        methods.add(methodBuilder);
        return new BeginCodeGenerationStrategy();
    }
}
