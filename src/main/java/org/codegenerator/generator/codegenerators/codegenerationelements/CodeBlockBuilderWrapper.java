package org.codegenerator.generator.codegenerators.codegenerationelements;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class CodeBlockBuilderWrapper {
    private static final String GENERIC = "#G";
    private static final String CODE_RGX = "\\$L";

    private final CodeBlock codeBlock;
    private final List<TypeName> generic;

    public CodeBlockBuilderWrapper(CodeBlock codeBlock) {
        this(codeBlock, new ArrayList<>());
    }

    public CodeBlockBuilderWrapper(CodeBlock codeBlock, List<TypeName> generic) {
        this.codeBlock = codeBlock;
        this.generic = generic;
    }

    public CodeBlock getCodeBlock() {
        return codeBlock;
    }

    public List<TypeName> getGeneric() {
        return generic;
    }

    public CodeBlockBuilderWrapper addGeneric(TypeName typeName) {
        generic.add(typeName);
        return this;
    }

    public CodeBlockBuilderWrapper removeGeneric(TypeName typeName) {
        generic.remove(typeName);
        return this;
    }

    public CodeBlockBuilderWrapper replaceOrAddGeneric(TypeName target, TypeName replacement) {
        return removeGeneric(target).addGeneric(replacement);
    }

    public CodeBlock build() {
        String format = codeBlock
                .toString()
                .replaceFirst(GENERIC, CODE_RGX);

        CodeBlock inner = CodeBlock.builder()
                .add(getGenericFormat(), generic.toArray())
                .build();

        return CodeBlock.builder()
                .add(format, inner)
                .build();
    }

    private String getGenericFormat() {
        StringJoiner stringJoiner = new StringJoiner(", ", "<", ">");
        generic.forEach(tn -> stringJoiner.add(tn.toString()));
        return stringJoiner.toString();
    }
}
