package org.codegenerator.history;

/**
 * Wrapper over method call and arguments
 *
 * @param <T> type of method
 */
public class HistoryCall<T> {
    private final History<T> history;
    private final T method;
    private final Object[] args;

    public HistoryCall(History<T> history, T method, Object... args) {
        this.method = method;
        this.history = history;
        this.args = args;
    }

    /**
     * Returns a method.
     *
     * @return method
     */
    public T getMethod() {
        return method;
    }

    /**
     * Returns the arguments with which the method was called.
     *
     * @return arguments with which the method was called
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * Returns the {@link HistoryNode<T>} argument with {@code index}.
     *
     * @return {@link HistoryNode<T>} argument with {@code index}
     */
    public HistoryNode<T> getHistoryArg(int index) {
        return history.get(args[index]);
    }
}
