package org.codegenerator.generator.graph.edges;

import java.lang.reflect.Constructor;

import static org.codegenerator.Utils.callSupplierWrapper;

public class EdgeConstructor implements Edge<Constructor<?>> {
    private final Constructor<?> constructor;
    private final Object[] args;

    public EdgeConstructor(Constructor<?> constructor, Object... args) {
        this.constructor = constructor;
        this.args = args;
    }

    @Override
    public Object invoke(Object object) {
        throw new UnsupportedOperationException();
    }

    public Object invoke() {
        return callSupplierWrapper(() -> constructor.newInstance(args));
    }

    public Constructor<?> getMethod() {
        return constructor;
    }

    public Object[] getArgs() {
        return args;
    }
}
