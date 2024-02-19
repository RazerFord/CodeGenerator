package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import org.apache.commons.lang3.NotImplementedException;
import org.codegenerator.generator.codegenerators.ContextGenerator;
import org.codegenerator.history.HistoryNode;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;

public class BeginCodeGenerationStrategy implements CodeGenerationStrategy {
    @Override
    public CodeGenerationStrategy generate(@NotNull ContextGenerator context) {
        HistoryNode<Executable> node = context.getStack().element().getFirst();
        switch (node.getType()) {
            case OBJECT:
                return StrategyFactory.getCodeGenerationStrategy(node);
            case ARRAY:
                return new ArrayCodeGenerationStrategy();
            case PRIMITIVE:
                return new PrimitiveCodeGenerationStrategy();
        }
        throw new NotImplementedException();
    }
}
