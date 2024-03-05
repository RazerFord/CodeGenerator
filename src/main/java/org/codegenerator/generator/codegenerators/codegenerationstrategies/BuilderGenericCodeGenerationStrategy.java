package org.codegenerator.generator.codegenerators.codegenerationstrategies;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import kotlin.Pair;
import org.codegenerator.Utils;
import org.codegenerator.generator.codegenerators.ContextGenerator;
import org.codegenerator.generator.codegenerators.codegenerationelements.GenericResolver;
import org.codegenerator.generator.methodsequencefinders.concrete.POJOMethodSequenceFinder;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryNode;
import org.codegenerator.history.HistoryObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.*;

import static org.codegenerator.generator.codegenerators.codegenerationstrategies.Utils.addGenericVariable;

public class BuilderGenericCodeGenerationStrategy implements CodeGenerationStrategy {
    private static final String BUILDER_VARIABLE_NAME = "builder";
    private static final String VARIABLE_NAME = "object";

    private final ReflectionCodeGeneration reflectionCodeGeneration = new ReflectionCodeGeneration();
    private final String builderVariableName;
    private final String variableName;

    public BuilderGenericCodeGenerationStrategy() {
        this(BUILDER_VARIABLE_NAME, VARIABLE_NAME);
    }

    public BuilderGenericCodeGenerationStrategy(String builderVariableName, String variableName) {
        this.builderVariableName = builderVariableName;
        this.variableName = variableName;
    }

    @Override
    public CodeGenerationStrategy generate(@NotNull ContextGenerator context) {
        HistoryNode<Executable> node = context.getStack().getFirst().getFirst();
        MethodSpec.Builder methodBuilder = context.getStack().getFirst().getSecond();
        GenericResolver resolver = context.getGenericResolver();
        Object builder = Utils.buildObject(node.getHistoryCalls());

        updateGenericResolver(builder, context.getGenericResolver(), context.getHistory(), node);
        addGenericVariable(resolver, node, methodBuilder);

        return generate(builder, resolver, context.getTypeBuilder(), context.getMethods(), context.getStack());
    }

    private @NotNull CodeGenerationStrategy generate(
            Object builder,
            GenericResolver resolver,
            TypeSpec.@NotNull Builder typeBuilder,
            @NotNull List<MethodSpec.Builder> methods,
            @NotNull Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack
    ) {
        Pair<HistoryNode<Executable>, MethodSpec.Builder> p = stack.pop();
        HistoryNode<Executable> historyNode = p.getFirst();
        MethodSpec.Builder methodBuilder = p.getSecond();

        List<Pair<? extends Statement, List<HistoryCall<Executable>>>> pairs = splitListIntoZones(
                historyNode.getObject(), builder, resolver, historyNode.getHistoryCalls(), new CallCreator(methods, stack)
        );

        for (Pair<? extends Statement, List<HistoryCall<Executable>>> pair : pairs) {
            process(pair, methodBuilder);
        }
        reflectionCodeGeneration.generate(variableName, typeBuilder, methods, p, stack);
        methodBuilder.addStatement("return $L", variableName);
        methods.add(methodBuilder);

        return new BeginCodeGenerationStrategy();
    }

    private @NotNull List<Pair<? extends Statement, List<HistoryCall<Executable>>>> splitListIntoZones(
            Object object,
            Object builder,
            GenericResolver resolver,
            @NotNull List<HistoryCall<Executable>> calls,
            CallCreator callCreator
    ) {
        int length = calls.size();
        return Arrays.asList(
                new Pair<>(new Init(callCreator, resolver, builder), calls.subList(0, 1)),
                new Pair<>(new Call(callCreator), calls.subList(1, length - 1)),
                new Pair<>(new Complete(variableName, callCreator, resolver, object), calls.subList(length - 1, length))
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
        private final GenericResolver resolver;
        private final Object builder;

        public Init(CallCreator callCreator, GenericResolver resolver, Object builder) {
            this.callCreator = callCreator;
            this.resolver = resolver;
            this.builder = builder;
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
                        .add("$T $L = new $T<>", resolver.resolve(builder), variableName, declaringClass)
                        .build();
            } else if (executable instanceof Method) {
                Method method = (Method) executable;
                String methodName = method.getName();
                codeBlock = CodeBlock.builder()
                        .add("$T $L = $T.$L", resolver.resolve(builder), variableName, declaringClass, methodName)
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
        private final GenericResolver resolver;
        private final Object builder;

        public Complete(
                String newVariableName,
                CallCreator callCreator,
                GenericResolver resolver,
                Object builder
        ) {
            this.newVariableName = newVariableName;
            this.callCreator = callCreator;
            this.resolver = resolver;
            this.builder = builder;
        }

        @Contract(pure = true)
        @Override
        public void append(
                CodeBlock.@NotNull Builder codeBlockBuilder,
                @NotNull HistoryCall<Executable> call,
                String variableName
        ) {
            Method method = (Method) call.getMethod();
            codeBlockBuilder.add("$T $L = $L.$L", resolver.resolve(builder), newVariableName, variableName, method.getName())
                    .add(callCreator.create(call));
        }
    }

    private void updateGenericResolver(
            Object builder,
            @NotNull GenericResolver resolver,
            History<Executable> history,
            @NotNull HistoryNode<Executable> node
    ) {
        updateHistory(history, node.getHistoryCalls(), builder);
        resolver.resolve(builder);
    }

    private void updateHistory(
            History<Executable> history,
            @NotNull List<HistoryCall<Executable>> calls,
            Object builder
    ) {
        List<HistoryCall<Executable>> calls1 = new ArrayList<>();
        for (int i = 0; i < calls.size() - 1; i++) {
            HistoryCall<Executable> call = calls.get(i);
            calls1.add(new HistoryCall<>(history, call.getMethod(), call.getArgs()));
        }
        history.put(builder, new HistoryObject<>(builder, calls1, Collections.emptyList(), POJOMethodSequenceFinder.class));
    }
}
