package org.codegenerator.generator.codegenerators;

import java.lang.reflect.Method;

public final class MethodCall {
    private final Method method;
    private final Object[] args;

    public MethodCall(Method method, Object... args) {
        this.method = method;
        this.args = args;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }
}

