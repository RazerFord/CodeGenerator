package org.codegenerator.generator.codegenerators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import kotlin.Pair;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class ContextGenerator {
    private final TypeSpec.Builder typeBuilder;
    private final List<MethodSpec.Builder> methods;
    private final Map<TypeName, TypeName> typeByParameter;
    private final Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack;
    private final History<Executable> history;

    public ContextGenerator(
            TypeSpec.Builder typeBuilder,
            List<MethodSpec.Builder> methods,
            Map<TypeName, TypeName> typeByParameter,
            Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack,
            History<Executable> history
    ) {
        this.typeBuilder = typeBuilder;
        this.methods = methods;
        this.typeByParameter = typeByParameter;
        this.stack = stack;
        this.history = history;
    }

    public TypeSpec.Builder getTypeBuilder() {
        return typeBuilder;
    }

    public List<MethodSpec.Builder> getMethods() {
        return methods;
    }

    public Map<TypeName, TypeName> getTypeByParameter() {
        return typeByParameter;
    }

    public Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> getStack() {
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
        private Map<TypeName, TypeName> typeByParameter;
        private Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack;
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

        public Map<TypeName, TypeName> getTypeByParameter() {
            return typeByParameter;
        }

        public Builder setTypeByParameter(Map<TypeName, TypeName> typeByParameter) {
            this.typeByParameter = typeByParameter;
            return this;
        }

        public Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> getStack() {
            return stack;
        }

        public Builder setStack(Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack) {
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
            return new ContextGenerator(typeBuilder, methods, typeByParameter, stack, history);
        }
    }
}
