package org.codegenerator.generator.codegenerators.codegenerationelements;

import com.squareup.javapoet.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenericMethodBuilder {
    private final List<GenericCodeBlockBuilder> blockBuilderWrappers = new ArrayList<>();
    private final MethodSpec.Builder methodBuilder;

    public GenericMethodBuilder(MethodSpec.Builder methodBuilder) {
        this.methodBuilder = methodBuilder;
    }

    public GenericMethodBuilder setName(String name) {
        methodBuilder.setName(name);
        return this;
    }

    public GenericMethodBuilder addJavadoc(String format, Object... args) {
        methodBuilder.addJavadoc(format, args);
        return this;
    }

    public GenericMethodBuilder addJavadoc(CodeBlock block) {
        methodBuilder.addJavadoc(block);
        return this;
    }

    public GenericMethodBuilder addAnnotations(Iterable<AnnotationSpec> annotationSpecs) {
        methodBuilder.addAnnotations(annotationSpecs);
        return this;
    }

    public GenericMethodBuilder addAnnotation(AnnotationSpec annotationSpec) {
        methodBuilder.addAnnotation(annotationSpec);
        return this;
    }

    public GenericMethodBuilder addAnnotation(ClassName annotation) {
        methodBuilder.addAnnotation(annotation);
        return this;
    }

    public GenericMethodBuilder addAnnotation(Class<?> annotation) {
        return addAnnotation(ClassName.get(annotation));
    }

    public GenericMethodBuilder addModifiers(Modifier... modifiers) {
        methodBuilder.addModifiers(modifiers);
        return this;
    }

    public GenericMethodBuilder addModifiers(Iterable<Modifier> modifiers) {
        methodBuilder.addModifiers(modifiers);
        return this;
    }

    public GenericMethodBuilder addTypeVariables(Iterable<TypeVariableName> typeVariables) {
        methodBuilder.addTypeVariables(typeVariables);
        return this;
    }

    public GenericMethodBuilder addTypeVariable(TypeVariableName typeVariable) {
        methodBuilder.addTypeVariable(typeVariable);
        return this;
    }

    public GenericMethodBuilder returns(TypeName returnType) {
        methodBuilder.returns(returnType);
        return this;
    }

    public GenericMethodBuilder returns(Type returnType) {
        methodBuilder.returns(returnType);
        return this;
    }

    public GenericMethodBuilder addParameters(Iterable<ParameterSpec> parameterSpecs) {
        methodBuilder.addParameters(parameterSpecs);
        return this;
    }

    public GenericMethodBuilder addParameter(ParameterSpec parameterSpec) {
        methodBuilder.addParameter(parameterSpec);
        return this;
    }

    public GenericMethodBuilder addParameter(TypeName type, String name, Modifier... modifiers) {
        methodBuilder.addParameter(type, name, modifiers);
        return this;
    }

    public GenericMethodBuilder addParameter(Type type, String name, Modifier... modifiers) {
        methodBuilder.addParameter(type, name, modifiers);
        return this;
    }

    public GenericMethodBuilder varargs() {
        methodBuilder.varargs();
        return this;
    }

    public GenericMethodBuilder varargs(boolean varargs) {
        methodBuilder.varargs(varargs);
        return this;
    }

    public GenericMethodBuilder addExceptions(Iterable<? extends TypeName> exceptions) {
        methodBuilder.addExceptions(exceptions);
        return this;
    }

    public GenericMethodBuilder addException(TypeName exception) {
        methodBuilder.addException(exception);
        return this;
    }

    public GenericMethodBuilder addException(Type exception) {
        methodBuilder.addException(exception);
        return this;
    }

    public GenericMethodBuilder addCode(String format, Object... args) {
//        methodBuilder.addCode(format, args);
        blockBuilderWrappers.add(new GenericCodeBlockBuilder(CodeBlock.builder().add(format, args)));
        return this;
    }

    public GenericMethodBuilder addNamedCode(String format, Map<String, ?> args) {
//        methodBuilder.addNamedCode(format, args);
        blockBuilderWrappers.add(new GenericCodeBlockBuilder(CodeBlock.builder().addNamed(format, args)));
        return this;
    }

    public GenericMethodBuilder addCode(@NotNull CodeBlock codeBlock) {
//        methodBuilder.addCode(codeBlock);
        blockBuilderWrappers.add(new GenericCodeBlockBuilder(codeBlock.toBuilder()));
        return this;
    }

    public GenericMethodBuilder addComment(String format, Object... args) {
        return addCode("// " + format + "\n", args);
    }

    public GenericMethodBuilder defaultValue(String format, Object... args) {
        methodBuilder.defaultValue(format, args);
        return this;
    }

    public GenericMethodBuilder defaultValue(CodeBlock codeBlock) {
        methodBuilder.defaultValue(codeBlock);
        return this;
    }

    public GenericMethodBuilder beginControlFlow(String controlFlow, Object... args) {
//        methodBuilder.beginControlFlow(controlFlow, args);
        blockBuilderWrappers.add(new GenericCodeBlockBuilder(CodeBlock.builder().beginControlFlow(controlFlow, args)));
        return this;
    }

    public GenericMethodBuilder beginControlFlow(@NotNull CodeBlock codeBlock) {
//        methodBuilder.beginControlFlow(codeBlock);
        return beginControlFlow("$L", codeBlock);
    }

    public GenericMethodBuilder nextControlFlow(String controlFlow, Object... args) {
//        methodBuilder.nextControlFlow(controlFlow, args);
        blockBuilderWrappers.add(new GenericCodeBlockBuilder(CodeBlock.builder().nextControlFlow(controlFlow, args)));
        return this;
    }

    public GenericMethodBuilder nextControlFlow(CodeBlock codeBlock) {
//        methodBuilder.nextControlFlow(codeBlock);
        return nextControlFlow("$L", codeBlock);
    }

    public GenericMethodBuilder endControlFlow() {
//        methodBuilder.endControlFlow();
        blockBuilderWrappers.add(new GenericCodeBlockBuilder(CodeBlock.builder().endControlFlow()));
        return this;
    }

    public GenericMethodBuilder endControlFlow(String controlFlow, Object... args) {
//        methodBuilder.endControlFlow(controlFlow, args);
        blockBuilderWrappers.add(new GenericCodeBlockBuilder(CodeBlock.builder().endControlFlow(controlFlow, args)));
        return this;
    }

    public GenericMethodBuilder endControlFlow(CodeBlock codeBlock) {
        return endControlFlow("$L", codeBlock);
    }

    public GenericMethodBuilder addStatement(String format, Object... args) {
//        methodBuilder.addStatement(format, args);
        blockBuilderWrappers.add(new GenericCodeBlockBuilder(CodeBlock.builder().addStatement(format, args)));
        return this;
    }

    public GenericMethodBuilder addStatement(CodeBlock codeBlock) {
//        methodBuilder.addStatement(codeBlock);
        blockBuilderWrappers.add(new GenericCodeBlockBuilder(CodeBlock.builder().addStatement(codeBlock)));
        return this;
    }

    public MethodSpec build() {
        MethodSpec.Builder builder = copy(methodBuilder.build());
        blockBuilderWrappers.forEach(cb -> builder.addCode(cb.build()));
        return builder.build();
    }

    private static MethodSpec.@NotNull Builder copy(@NotNull MethodSpec method) {
        MethodSpec.Builder copy = MethodSpec.methodBuilder(method.name);
        copy.addJavadoc(copy(method.javadoc));
        copy.annotations.addAll(method.annotations);
        copy.modifiers.addAll(method.modifiers);
        copy.typeVariables.addAll(method.typeVariables);
        copy.returns(method.returnType);
        copy.parameters.addAll(method.parameters);
        copy.addExceptions(method.exceptions);
        copy.addCode(copy(method.code));
        copy.varargs(method.varargs);
        copy.defaultValue(copy(method.defaultValue));
        return copy;
    }

    @Contract("_ -> new")
    private static @NotNull CodeBlock copy(CodeBlock block) {
        return CodeBlock.builder()
                .add(block)
                .build();
    }
}
