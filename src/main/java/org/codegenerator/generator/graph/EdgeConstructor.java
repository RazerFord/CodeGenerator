package org.codegenerator.generator.graph;

import java.lang.reflect.Constructor;

import static org.codegenerator.Utils.callSupplierWrapper;

public class EdgeConstructor {
    private final Constructor<?> constructor;
    private final Object[] args;

    public EdgeConstructor(Constructor<?> constructor, Object... args) {
        this.constructor = constructor;
        this.args = args;
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
