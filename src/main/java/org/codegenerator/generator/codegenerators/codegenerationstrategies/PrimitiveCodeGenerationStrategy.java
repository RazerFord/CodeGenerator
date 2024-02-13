package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import kotlin.Pair;
import org.codegenerator.generator.converters.ConverterPrimitiveTypesAndString;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryNode;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Deque;

public class PrimitiveCodeGenerationStrategy implements CodeGenerationStrategy {
    @Override
    public CodeGenerationStrategy generate(
            TypeSpec.@NotNull Builder typeBuilder,
            @NotNull Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack,
            History<Executable> history
    ) {
        Pair<HistoryNode<Executable>, MethodSpec.Builder> p = stack.pop();
        Object object = p.getFirst().getObject();
        MethodSpec.Builder methodBuilder = p.getSecond();

        methodBuilder.addStatement("return $L", ConverterPrimitiveTypesAndString.convert(object));
        typeBuilder.addMethod(methodBuilder.build());
        return new BeginCodeGenerationStrategy();
    }
}
