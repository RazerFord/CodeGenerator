package org.codegenerator.generator.codegenerators;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import kotlin.Pair;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Deque;

public class ContextGenerator {
    private final TypeSpec.Builder typeBuilder;
    private final Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack;
    private final History<Executable> history;

    public ContextGenerator(
            TypeSpec.Builder typeBuilder,
            Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack,
            History<Executable> history
    ) {
        this.typeBuilder = typeBuilder;
        this.stack = stack;
        this.history = history;
    }

    public TypeSpec.Builder getTypeBuilder() {
        return typeBuilder;
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
        private Deque<Pair<HistoryNode<Executable>, MethodSpec.Builder>> stack;
        private History<Executable> history;

        public TypeSpec.Builder getTypeBuilder() {
            return typeBuilder;
        }

        public Builder setTypeBuilder(TypeSpec.Builder typeBuilder) {
            this.typeBuilder = typeBuilder;
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
            return new ContextGenerator(typeBuilder, stack, history);
        }
    }
}
