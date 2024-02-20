package org.codegenerator.generator.codegenerators.codegenerationelements;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class GenericCodeBlockBuilder {
    private static final String GENERIC_PLACE = "#G";
    private static final String CODE_PLACE = "\\$L";

    private final List<Consumer<CodeBlock.Builder>> processing = new ArrayList<>();
    private final CodeBlock.Builder codeBlockBuilder;
    private final Map<Integer, List<TypeName>> genericParameters;

    public GenericCodeBlockBuilder() {
        this(CodeBlock.builder(), new HashMap<>());
    }

    public GenericCodeBlockBuilder(CodeBlock.Builder codeBlockBuilder) {
        this(codeBlockBuilder, new HashMap<>());
    }

    public GenericCodeBlockBuilder(CodeBlock.Builder codeBlockBuilder, Map<Integer, List<TypeName>> generic) {
        this.codeBlockBuilder = codeBlockBuilder;
        this.genericParameters = generic;
    }

    public GenericCodeBlockBuilder putGenerics(int index, List<TypeName> typeNames) {
        genericParameters.put(index, typeNames);
        return this;
    }

    public GenericCodeBlockBuilder removeGenerics(int index) {
        genericParameters.remove(index);
        return this;
    }

    public GenericCodeBlockBuilder replaceOrAddGeneric(int index, TypeName target, TypeName replacement) {
        List<TypeName> typeNames = genericParameters.get(index);
        if ((index = typeNames.indexOf(target)) == -1) {
            typeNames.add(replacement);
        } else {
            typeNames.set(index, replacement);
        }
        return this;
    }

    public boolean isEmpty() {
        return codeBlockBuilder.isEmpty() || processing.isEmpty();
    }

    public GenericCodeBlockBuilder addNamed(@NotNull String format, Map<String, ?> arguments) {
        processing.add(cb -> cb.addNamed(format, arguments));
        return this;
    }

    public GenericCodeBlockBuilder add(String format, Object... args) {
        processing.add(cb -> processGenerics(CodeBlock.Builder::add, cb, format, args));
        return this;
    }

    public GenericCodeBlockBuilder beginControlFlow(String controlFlow, Object... args) {
        processing.add(cb -> processGenerics(CodeBlock.Builder::beginControlFlow, cb, controlFlow, args));
        return this;
    }

    public GenericCodeBlockBuilder nextControlFlow(String controlFlow, Object... args) {
        processing.add(cb -> processGenerics(CodeBlock.Builder::nextControlFlow, cb, controlFlow, args));
        return this;
    }

    public GenericCodeBlockBuilder endControlFlow() {
        processing.add(CodeBlock.Builder::endControlFlow);
        return this;
    }

    public GenericCodeBlockBuilder endControlFlow(String controlFlow, Object... args) {
        processing.add(cb -> processGenerics(CodeBlock.Builder::endControlFlow, cb, controlFlow, args));
        return this;
    }

    public GenericCodeBlockBuilder addStatement(String format, Object... args) {
        processing.add(cb -> processGenerics(CodeBlock.Builder::addStatement, cb, format, args));
        return this;
    }

    public GenericCodeBlockBuilder addStatement(CodeBlock codeBlock) {
        processing.add(cb -> cb.addStatement(codeBlock));
        return this;
    }

    public GenericCodeBlockBuilder add(CodeBlock codeBlock) {
        processing.add(cb -> cb.add(codeBlock));
        return this;
    }

    public GenericCodeBlockBuilder indent() {
        processing.add(CodeBlock.Builder::indent);
        return this;
    }

    public GenericCodeBlockBuilder unindent() {
        processing.add(CodeBlock.Builder::unindent);
        return this;
    }

    public GenericCodeBlockBuilder clear() {
        processing.add(CodeBlock.Builder::clear);
        return this;
    }

    public CodeBlock.Builder getCodeBlockBuilder() {
        return codeBlockBuilder;
    }

    public Map<Integer, List<TypeName>> getGenericParameters() {
        return genericParameters;
    }

    public CodeBlock build() {
        CodeBlock.Builder builder = CodeBlock.builder().add(codeBlockBuilder.build());
        processing.forEach(consumer -> consumer.accept(builder));
        return builder.build();
    }

    private void processGenerics(
            TriConsumer<CodeBlock.Builder, String, Object[]> consumer,
            CodeBlock.Builder codeBlockBuilder,
            @NotNull String format,
            Object... args
    ) {
        if (format.contains(GENERIC_PLACE)) {
            // TODO
        }
        consumer.accept(codeBlockBuilder, format, args);
    }

    private static String getGenericFormat(@NotNull List<TypeName> genericParameters) {
        StringJoiner stringJoiner = new StringJoiner(", ", "<", ">");
        genericParameters.forEach(tn -> stringJoiner.add(tn.toString()));
        return stringJoiner.toString();
    }
}
