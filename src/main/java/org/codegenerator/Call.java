package org.codegenerator;

public class Call<T> {
    private final T method;
    private final Object[] args;

    public Call(T method, Object... args) {
        this.method = method;
        this.args = args;
    }

    public T getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }
}
