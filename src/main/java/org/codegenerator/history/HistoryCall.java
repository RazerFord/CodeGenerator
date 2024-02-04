package org.codegenerator.history;

public class HistoryCall<T> {
    private final History<T> history;
    private final T method;
    private final Object[] args;

    public HistoryCall(History<T> history, T method, Object... args) {
        this.method = method;
        this.history = history;
        this.args = args;
    }

    public T getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    public HistoryObject<T> getHistoryArg(int index) {
        return history.get(args[index]);
    }
}
