package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.MethodSpec;
import kotlin.Pair;
import org.codegenerator.generator.codegenerators.ContextGenerator;
import org.codegenerator.generator.converters.PrimitiveConverter;
import org.codegenerator.history.HistoryNode;
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
            @NotNull Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack
    ) {
        Pair<HistoryNode<Executable>, MethodSpec.Builder> p = stack.pop();
        Object object = p.getFirst().getObject();
        MethodSpec.Builder methodBuilder = p.getSecond();

        methodBuilder.addStatement("return $L", PrimitiveConverter.convert(object));
        methods.add(methodBuilder);
        return new BeginCodeGenerationStrategy();
    }
}
