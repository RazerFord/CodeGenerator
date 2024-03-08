package org.codegenerator.generator.codegenerators;

import com.squareup.javapoet.MethodSpec;
import org.codegenerator.history.HistoryNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Executable;
import java.util.Collections;
import java.util.Set;

public class MethodContext<T> {
    private final MethodSpec.Builder method;
    private final HistoryNode<T> node;
    private final String variableName;
    private final Set<String> variables;
    private final boolean added;

    public MethodContext(MethodSpec.Builder method, HistoryNode<T> node) {
        this(method, node, null, false, Collections.emptySet());
    }

    @Contract(pure = true)
    public MethodContext(
            MethodSpec.Builder method,
            HistoryNode<T> node,
            String variableName,
            @NotNull MethodContext<Executable> parent
    ) {
        this(method, node, variableName, true, parent.variables);
    }

    public MethodContext(
            MethodSpec.Builder method,
            HistoryNode<T> node,
            String variableName,
            boolean added,
            Set<String> variables
    ) {
        this.method = method;
        this.node = node;
        this.variableName = variableName;
        this.added = added;
        this.variables = variables;
    }

    public MethodSpec.Builder getMethod() {
        return method;
    }

    public HistoryNode<T> getNode() {
        return node;
    }

    public String getVariableName() {
        return variableName;
    }

    public Set<String> getVariables() {
        return variables;
    }

    public boolean isAdded() {
        return added;
    }
}
