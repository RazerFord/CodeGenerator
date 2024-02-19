package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import kotlin.Pair;
import org.codegenerator.generator.codegenerators.ContextGenerator;
import org.codegenerator.generator.converters.PrimitiveConverter;
import org.codegenerator.history.HistoryNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Deque;

public class PrimitiveCodeGenerationStrategy implements CodeGenerationStrategy {
    @Override
    public CodeGenerationStrategy generate(@NotNull ContextGenerator context) {
        return generate(context.getTypeBuilder(), context.getStack());
    }

    @Contract("_, _ -> new")
    private @NotNull CodeGenerationStrategy generate(
            TypeSpec.@NotNull Builder typeBuilder,
            @NotNull Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack
    ) {
        Pair<HistoryNode<Executable>, MethodSpec.Builder> p = stack.pop();
        Object object = p.getFirst().getObject();
        MethodSpec.Builder methodBuilder = p.getSecond();

        methodBuilder.addStatement("return $L", PrimitiveConverter.convert(object));
        typeBuilder.addMethod(methodBuilder.build());
        return new BeginCodeGenerationStrategy();
    }
}
