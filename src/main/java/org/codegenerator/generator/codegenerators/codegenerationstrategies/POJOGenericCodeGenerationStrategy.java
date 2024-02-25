package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.*;
import kotlin.Pair;
import org.codegenerator.generator.codegenerators.ContextGenerator;
import org.codegenerator.generator.codegenerators.codegenerationelements.GenericResolver;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Deque;
import java.util.List;

import static org.codegenerator.generator.codegenerators.codegenerationstrategies.Utils.addGenericVariable;

public class POJOGenericCodeGenerationStrategy implements CodeGenerationStrategy {
    private static final String VARIABLE_NAME = "object";

    private final ReflectionCodeGeneration reflectionCodeGeneration = new ReflectionCodeGeneration();
    private final String variableName;

    public POJOGenericCodeGenerationStrategy() {
        this(VARIABLE_NAME);
    }

    public POJOGenericCodeGenerationStrategy(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public CodeGenerationStrategy generate(@NotNull ContextGenerator context) {
        return generate(context.getGenericResolver(), context.getTypeBuilder(), context.getMethods(), context.getStack());
    }

    private @NotNull CodeGenerationStrategy generate(
            GenericResolver resolver,
            TypeSpec.@NotNull Builder typeBuilder,
            @NotNull List<MethodSpec.Builder> methods,
            @NotNull Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack
    ) {
        Pair<HistoryNode<Executable>, MethodSpec.Builder> p = stack.pop();
        HistoryNode<Executable> historyNode = p.getFirst();
        MethodSpec.Builder methodBuilder = p.getSecond();

        addGenericVariable(resolver, historyNode, methodBuilder);

        Statement statement = new Init(new CallCreator(methods, stack), resolver, historyNode);

        for (HistoryCall<Executable> call : historyNode.getHistoryCalls()) {
            CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
            statement = statement.append(codeBlockBuilder, call, variableName);
            methodBuilder.addStatement(codeBlockBuilder.build());
        }
        reflectionCodeGeneration.generate(variableName, typeBuilder, methods, p, stack);
        methodBuilder.addStatement("return $L", variableName)
                .returns(resolver.resolve(historyNode.getObject()));

        methods.add(methodBuilder);
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

    private static class Init implements Statement {
        private final CallCreator callCreator;
        private final GenericResolver resolver;
        private final HistoryNode<Executable> node;

        public Init(
                CallCreator callCreator,
                GenericResolver resolver,
                HistoryNode<Executable> node
        ) {
            this.callCreator = callCreator;
            this.resolver = resolver;
            this.node = node;
        }


        @Contract(pure = true)
        @Override
        public @NotNull Statement append(
                CodeBlock.@NotNull Builder codeBlockBuilder,
                @NotNull HistoryCall<Executable> call,
                String variableName
        ) {
            codeBlockBuilder.add("$T $L = new $T<>", resolver.resolve(node), variableName, call.getMethod().getDeclaringClass())
                    .add(callCreator.create(call));
            return new Call(callCreator);
        }
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
