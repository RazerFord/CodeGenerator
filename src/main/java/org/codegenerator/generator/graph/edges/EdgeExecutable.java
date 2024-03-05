package org.codegenerator.generator.graph.edges;

import org.jacodb.api.JcField;
import org.jacodb.api.JcLookup;
import org.jacodb.api.JcMethod;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;

public class EdgeExecutable implements Edge<Executable> {
    private final Edge<? extends Executable> executable;

    public EdgeExecutable(Executable executable, Object... args) {
        if (executable instanceof Constructor<?>) {
            this.executable = new EdgeConstructor((Constructor<?>) executable, args);
        } else if (executable instanceof Method) {
            this.executable = new EdgeMethod((Method) executable, args);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Object invoke(Object object) {
        return executable.invoke(object);
    }

    @Override
    public Object invoke() {
        return executable.invoke();
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
