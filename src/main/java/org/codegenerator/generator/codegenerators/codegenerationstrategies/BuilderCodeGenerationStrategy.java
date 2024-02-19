package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import kotlin.Pair;
import org.codegenerator.generator.codegenerators.ContextGenerator;
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
    public CodeGenerationStrategy generate(@NotNull ContextGenerator context) {
        return generate(context.getTypeBuilder(), context.getStack());
    }

    @Contract("_, _ -> new")
    private @NotNull CodeGenerationStrategy generate(
            TypeSpec.@NotNull Builder typeBuilder,
            @NotNull Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack
    ) {
        Pair<HistoryNode<Executable>, MethodSpec.Builder> p = stack.pop();
        HistoryNode<Executable> historyNode = p.getFirst();
        MethodSpec.Builder methodBuilder = p.getSecond();

        List<Pair<? extends Statement, List<HistoryCall<Executable>>>> pairs = splitListIntoZones(historyNode.getHistoryCalls(), new CallCreator(typeBuilder, stack));

        for (Pair<? extends Statement, List<HistoryCall<Executable>>> pair : pairs) {
            process(pair, methodBuilder);
        }
        reflectionCodeGeneration.generate(variableName, typeBuilder, p, stack);
        methodBuilder.addStatement("return $L", variableName);
        typeBuilder.addMethod(methodBuilder.build());
        return new BeginCodeGenerationStrategy();
    }

    private @NotNull List<Pair<? extends Statement, List<HistoryCall<Executable>>>> splitListIntoZones(
            @NotNull List<HistoryCall<Executable>> calls,
            CallCreator callCreator
    ) {
        int length = calls.size();
        return Arrays.asList(
                new Pair<>(new Init(callCreator), calls.subList(0, 1)),
                new Pair<>(new Call(callCreator), calls.subList(1, length - 1)),
                new Pair<>(new Complete(variableName, callCreator), calls.subList(length - 1, length))
        );
    }

    private void process(
            @NotNull Pair<? extends Statement, List<HistoryCall<Executable>>> pair,
            MethodSpec.Builder methodBuilder
    ) {
        Statement statement = pair.getFirst();

        for (HistoryCall<Executable> call : pair.getSecond()) {
            CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();

            statement.append(codeBlockBuilder, call, builderVariableName);

            methodBuilder.addStatement(codeBlockBuilder.build());
        }
    }

    @FunctionalInterface
    private interface Statement {
        void append(
                CodeBlock.Builder codeBlockBuilder,
                HistoryCall<Executable> call,
                String variableName
        );
    }

    private static class Init implements Statement {
        private final CallCreator callCreator;

        public Init(CallCreator callCreator) {
            this.callCreator = callCreator;
        }

        @Contract(pure = true)
        @Override
        public void append(
                CodeBlock.@NotNull Builder codeBlockBuilder,
                @NotNull HistoryCall<Executable> call,
                String variableName
        ) {
            CodeBlock codeBlock;
            Executable executable = call.getMethod();
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
            codeBlockBuilder.add(codeBlock)
                    .add(callCreator.create(call));
        }
    }

    private static class Call implements Statement {
        private final CallCreator callCreator;

        public Call(CallCreator callCreator) {
            this.callCreator = callCreator;
        }

        @Contract(pure = true)
        @Override
        public void append(
                CodeBlock.@NotNull Builder codeBlockBuilder,
                @NotNull HistoryCall<Executable> call,
                String variableName
        ) {
            codeBlockBuilder.add("$L.$L", variableName, call.getMethod().getName())
                    .add(callCreator.create(call));
        }
    }

    private static class Complete implements Statement {
        private final String newVariableName;
        private final CallCreator callCreator;

        public Complete(String newVariableName, CallCreator callCreator) {
            this.newVariableName = newVariableName;
            this.callCreator = callCreator;
        }

        @Contract(pure = true)
        @Override
        public void append(
                CodeBlock.@NotNull Builder codeBlockBuilder,
                @NotNull HistoryCall<Executable> call,
                String variableName
        ) {
            Method method = (Method) call.getMethod();
            codeBlockBuilder.add("$T $L = $L.$L", method.getReturnType(), newVariableName, variableName, method.getName())
                    .add(callCreator.create(call));
        }
    }
}
