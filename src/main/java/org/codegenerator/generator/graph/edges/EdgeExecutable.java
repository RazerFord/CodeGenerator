package org.codegenerator.generator.graph.edges;

import org.jacodb.api.JcField;
import org.jacodb.api.JcLookup;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.function.Function;

public class EdgeExecutable implements Edge<Executable> {
    private final boolean isConstructor;
    private final Edge<? extends Executable> executable;
    private final Function<Object, Object> invoker;

    public EdgeExecutable(Executable executable, Object... args) {
        if (executable instanceof Constructor<?>) {
            isConstructor = true;
            this.executable = new EdgeConstructor((Constructor<?>) executable, args);
            invoker = o -> this.executable.invoke();
        } else if (executable instanceof Method) {
            isConstructor = false;
            this.executable = new EdgeMethod((Method) executable, args);
            invoker = this.executable::invoke;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    @Override
    public Object invoke(Object object) {
        return invoker.apply(object);
    }

    @Override
    public Object invoke() {
        return invoker.apply(null);
    }

    @Override
    public Executable getMethod() {
        return executable.getMethod();
    }

    @Override
    public Object[] getArgs() {
        return executable.getArgs();
    }

    @Override
    public JcMethod toJcMethod(@NotNull JcLookup<JcField, JcMethod> lookup) {
        return executable.toJcMethod(lookup);
    }
}
