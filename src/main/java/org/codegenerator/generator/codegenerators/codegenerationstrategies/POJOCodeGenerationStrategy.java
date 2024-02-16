package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import kotlin.Pair;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Deque;

public class POJOCodeGenerationStrategy implements CodeGenerationStrategy {
    private static final String VARIABLE_NAME = "object";

    private final ReflectionCodeGeneration reflectionCodeGeneration = new ReflectionCodeGeneration();
    private final String variableName;

    public POJOCodeGenerationStrategy() {
        this(VARIABLE_NAME);
    }

    public POJOCodeGenerationStrategy(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public CodeGenerationStrategy generate(
            TypeSpec.@NotNull Builder typeBuilder,
            @NotNull Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack,
            History<Executable> history
    ) {
        Pair<HistoryNode<Executable>, MethodSpec.Builder> p = stack.pop();
        HistoryNode<Executable> historyNode = p.getFirst();
        MethodSpec.Builder methodBuilder = p.getSecond();

        Statement statement = new Init();

        for (HistoryCall<Executable> call : historyNode.getHistoryCalls()) {
            CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
            statement = statement.append(codeBlockBuilder, call.getMethod(), variableName);

            String suffix = String.valueOf(typeBuilder.methodSpecs.size() + stack.size());
            CodeBlock codeBlock = codeBlockBuilder.add(Utils.createCall(suffix, stack, call)).build();

            methodBuilder.addStatement(codeBlock);
        }
        reflectionCodeGeneration.generate(variableName, typeBuilder, p, stack);
        methodBuilder.addStatement("return $L", variableName);

        typeBuilder.addMethod(methodBuilder.build());
        return new BeginCodeGenerationStrategy();
    }

    @FunctionalInterface
    private interface Statement {
        Statement append(
                CodeBlock.Builder codeBlockBuilder,
                Executable executable,
                String variableName
        );
    }

    private static class Init implements Statement {
        @Contract(pure = true)
        @Override
        public @NotNull Statement append(
                CodeBlock.@NotNull Builder codeBlockBuilder,
                @NotNull Executable executable,
                String variableName
        ) {
            codeBlockBuilder.add("$1T $2L = new $1T", executable.getDeclaringClass(), variableName);
            return new Call();
        }
    }

    private static class Call implements Statement {
        @Contract(pure = true)
        @Override
        public @NotNull Statement append(
                CodeBlock.@NotNull Builder codeBlockBuilder,
                @NotNull Executable executable,
                String variableName
        ) {
            codeBlockBuilder.add("$L.$L", variableName, executable.getName());
            return this;
        }
    }
}
