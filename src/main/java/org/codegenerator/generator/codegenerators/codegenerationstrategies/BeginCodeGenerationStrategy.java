package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import kotlin.Pair;
import org.apache.commons.lang3.NotImplementedException;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryNode;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Deque;

public class BeginCodeGenerationStrategy implements CodeGenerationStrategy {
    @Override
    public CodeGenerationStrategy generate(
            TypeSpec.Builder typeBuilder,
            @NotNull Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack,
            History<Executable> history
    ) {
        HistoryNode<Executable> node = stack.element().getFirst();
        switch (node.getType()) {
            case OBJECT:
                return StrategyFactory.getCodeGenerationStrategy(node.getCreatorType());
            case ARRAY:
                return new ArrayCodeGenerationStrategy();
            case PRIMITIVE:
                return new PrimitiveCodeGenerationStrategy();
        }
        throw new NotImplementedException();
    }
}
