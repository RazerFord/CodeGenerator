package org.codegenerator.generator.graph;

import java.lang.reflect.Method;

import static org.codegenerator.Utils.callSupplierWrapper;

public final class EdgeMethod {
    private final Method method;
    private final Object[] args;

    public EdgeMethod(Method method, Object... args) {
        this.method = method;
        this.args = args;
    }

    public Object invoke(Object object) {
        return callSupplierWrapper(() -> method.invoke(object, args));
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }
}
