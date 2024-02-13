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

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class BuilderCodeGenerationStrategy implements CodeGenerationStrategy {
    private static final String BUILDER_VARIABLE_NAME = "builder";
    private static final String VARIABLE_NAME = "object";

    private final ReflectionCodeGeneration reflectionCodeGeneration = new ReflectionCodeGeneration();
    private final String builderVariableName;
    private final String variableName;

    public BuilderCodeGenerationStrategy() {
        this(BUILDER_VARIABLE_NAME, VARIABLE_NAME);
    }

    public BuilderCodeGenerationStrategy(String builderVariableName, String variableName) {
        this.builderVariableName = builderVariableName;
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

        List<Pair<? extends Statement, List<HistoryCall<Executable>>>> pairs = splitListIntoZones(historyNode.getHistoryCalls());

        for (Pair<? extends Statement, List<HistoryCall<Executable>>> pair : pairs) {
            process(pair, typeBuilder, methodBuilder, stack);
        }
        reflectionCodeGeneration.generate(variableName, typeBuilder, p, stack);
        methodBuilder.addStatement("return $L", variableName);
        typeBuilder.addMethod(methodBuilder.build());
        return new BeginCodeGenerationStrategy();
    }

    private @NotNull List<Pair<? extends Statement, List<HistoryCall<Executable>>>> splitListIntoZones(
            @NotNull List<HistoryCall<Executable>> calls
    ) {
        int length = calls.size();
        return Arrays.asList(
                new Pair<>(new Init(), calls.subList(0, 1)),
                new Pair<>(new Call(), calls.subList(1, length - 1)),
                new Pair<>(new Complete(variableName), calls.subList(length - 1, length))
        );
    }

    private void process(
            @NotNull Pair<? extends Statement, List<HistoryCall<Executable>>> pair,
            TypeSpec.Builder typeBuilder,
            MethodSpec.Builder methodBuilder,
            @NotNull Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack
    ) {
        Statement statement = pair.getFirst();

        for (HistoryCall<Executable> call : pair.getSecond()) {

            CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
            statement.append(codeBlockBuilder, call.getMethod(), builderVariableName);

            String suffix = String.valueOf(typeBuilder.methodSpecs.size() + stack.size());
            CodeBlock codeBlock = codeBlockBuilder.add(Utils.createCall(suffix, stack, call)).build();

            methodBuilder.addStatement(codeBlock);
        }
    }

    @FunctionalInterface
    private interface Statement {
        void append(
                CodeBlock.Builder codeBlockBuilder,
                Executable executable,
                String variableName
        );
    }

    private static class Init implements Statement {
        @Contract(pure = true)
        @Override
        public void append(
                CodeBlock.@NotNull Builder codeBlockBuilder,
                @NotNull Executable executable,
                String variableName
        ) {
            CodeBlock codeBlock;
            Class<?> declaringClass = executable.getDeclaringClass();
            if (executable instanceof Constructor<?>) {
                codeBlock = CodeBlock.builder()
                        .add("$1T $2L = new $1T", declaringClass, variableName)
                        .build();
            } else if (executable instanceof Method) {
                Method method = (Method) executable;
                Class<?> variableType = method.getReturnType();
                String methodName = method.getName();
                codeBlock = CodeBlock.builder()
                        .add("$T $L = $T.$L", variableType, variableName, declaringClass, methodName)
                        .build();
            } else {
                throw new IllegalArgumentException();
            }
            codeBlockBuilder.add(codeBlock);
        }
    }

    private static class Call implements Statement {
        @Contract(pure = true)
        @Override
        public void append(
                CodeBlock.@NotNull Builder codeBlockBuilder,
                @NotNull Executable executable,
                String variableName
        ) {
            codeBlockBuilder.add("$L.$L", variableName, executable.getName());
        }
    }

    private static class Complete implements Statement {
        private final String newVariableName;

        public Complete(String newVariableName) {
            this.newVariableName = newVariableName;
        }

        @Contract(pure = true)
        @Override
        public void append(
                CodeBlock.@NotNull Builder codeBlockBuilder,
                @NotNull Executable executable,
                String variableName
        ) {
            Method method = (Method) executable;
            codeBlockBuilder.add("$T $L = $L.$L", method.getReturnType(), newVariableName, variableName, executable.getName());
        }
    }
}
