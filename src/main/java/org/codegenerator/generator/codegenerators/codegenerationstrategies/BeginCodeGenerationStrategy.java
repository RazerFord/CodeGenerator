package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import org.apache.commons.lang3.NotImplementedException;
import org.codegenerator.generator.codegenerators.ContextGenerator;
import org.codegenerator.generator.codegenerators.MethodContext;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;

public class BeginCodeGenerationStrategy implements CodeGenerationStrategy {
    @Override
    public CodeGenerationStrategy generate(@NotNull ContextGenerator context) {
        MethodContext<Executable> methodContext = context.getStack().element();
        switch (methodContext.getNode().getType()) {
            case OBJECT:
                return StrategyFactory.getCodeGenerationStrategy(methodContext);
            case ARRAY:
                return new ArrayCodeGenerationStrategy();
            case PRIMITIVE:
                return new PrimitiveCodeGenerationStrategy();
        }
        throw new NotImplementedException();
    }
}
