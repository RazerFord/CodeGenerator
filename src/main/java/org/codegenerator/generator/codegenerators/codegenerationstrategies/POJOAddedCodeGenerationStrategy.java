package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.codegenerators.ContextGenerator;
import org.codegenerator.generator.codegenerators.MethodContext;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Deque;
import java.util.List;

public class POJOAddedCodeGenerationStrategy implements CodeGenerationStrategy {
    private final ReflectionCodeGeneration reflectionCodeGeneration = new ReflectionCodeGeneration();

    @Override
    public CodeGenerationStrategy generate(@NotNull ContextGenerator context) {
        return generate(context.getTypeBuilder(), context.getMethods(), context.getStack());
    }

    private @NotNull CodeGenerationStrategy generate(
            TypeSpec.@NotNull Builder typeBuilder,
            @NotNull List<MethodSpec.Builder> methods,
            @NotNull Deque<MethodContext<Executable>> stack
    ) {
        MethodContext<Executable> p = stack.pop();
        HistoryNode<Executable> historyNode = p.getNode();
        MethodSpec.Builder methodBuilder = p.getMethod();
        String variableName = p.getVariableName();

        Statement statement = new Call(new CallCreator(methods, stack));
        for (HistoryCall<Executable> call : historyNode.getHistoryCalls()) {
            CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
            statement = statement.append(codeBlockBuilder, call, variableName);
            methodBuilder.addStatement(codeBlockBuilder.build());
        }
        reflectionCodeGeneration.generate(variableName, typeBuilder, methods, p, stack);

        if (historyNode.nextNode() == null) {
            methodBuilder.addStatement("return $L", variableName);
            methods.add(methodBuilder);
        } else {
            stack.add(new MethodContext<>(methodBuilder, historyNode.nextNode(), variableName, p));
        }

        return new BeginCodeGenerationStrategy();
    }

    @FunctionalInterface
    private interface Statement {
        Statement append(
                CodeBlock.Builder codeBlockBuilder,
                HistoryCall<Executable> call,
                String variableName
        );
    }

    private static class Call implements Statement {
        private final CallCreator callCreator;

        public Call(CallCreator callCreator) {
            this.callCreator = callCreator;
        }

        @Contract(pure = true)
        @Override
        public @NotNull Statement append(
                CodeBlock.@NotNull Builder codeBlockBuilder,
                @NotNull HistoryCall<Executable> call,
                String variableName
        ) {
            codeBlockBuilder.add("$L.$L", variableName, call.getMethod().getName())
                    .add(callCreator.create(call));
            return this;
        }
    }
}
