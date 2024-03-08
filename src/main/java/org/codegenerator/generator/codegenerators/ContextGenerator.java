package org.codegenerator.generator.codegenerators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.codegenerator.generator.codegenerators.codegenerationelements.GenericResolver;
import org.codegenerator.history.History;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Deque;
import java.util.List;

public class ContextGenerator {
    private final TypeSpec.Builder typeBuilder;
    private final List<MethodSpec.Builder> methods;
    private final GenericResolver genericResolver;
    private final Deque<MethodContext<Executable>> stack;
    private final History<Executable> history;

    public ContextGenerator(
            TypeSpec.Builder typeBuilder,
            List<MethodSpec.Builder> methods,
            GenericResolver genericResolver,
            Deque<MethodContext<Executable>> stack,
            History<Executable> history
    ) {
        this.typeBuilder = typeBuilder;
        this.methods = methods;
        this.genericResolver = genericResolver;
        this.stack = stack;
        this.history = history;
    }

    public TypeSpec.Builder getTypeBuilder() {
        return typeBuilder;
    }

    public List<MethodSpec.Builder> getMethods() {
        return methods;
    }

    public GenericResolver getGenericResolver() {
        return genericResolver;
    }

    public Deque<MethodContext<Executable>> getStack() {
        return stack;
    }

    public History<Executable> getHistory() {
        return history;
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Builder() {
        }

        private TypeSpec.Builder typeBuilder;
        private List<MethodSpec.Builder> methods;
        private GenericResolver genericResolver;
        private Deque<MethodContext<Executable>> stack;
        private History<Executable> history;

        public TypeSpec.Builder getTypeBuilder() {
            return typeBuilder;
        }

        public Builder setTypeBuilder(TypeSpec.Builder typeBuilder) {
            this.typeBuilder = typeBuilder;
            return this;
        }

        public List<MethodSpec.Builder> getMethods() {
            return methods;
        }

        public Builder setMethods(List<MethodSpec.Builder> methods) {
            this.methods = methods;
            return this;
        }

        public GenericResolver getGenericResolver() {
            return genericResolver;
        }

        public Builder setGenericResolver(GenericResolver genericResolver) {
            this.genericResolver = genericResolver;
            return this;
        }

        public Deque<MethodContext<Executable>> getStack() {
            return stack;
        }

        public Builder setStack(Deque<MethodContext<Executable>> stack) {
            this.stack = stack;
            return this;
        }

        public History<Executable> getHistory() {
            return history;
        }

        public Builder setHistory(History<Executable> history) {
            this.history = history;
            return this;
        }

        public ContextGenerator build() {
            return new ContextGenerator(typeBuilder, methods, genericResolver, stack, history);
        }
    }
}
