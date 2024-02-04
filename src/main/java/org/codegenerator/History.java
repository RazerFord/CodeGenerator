package org.codegenerator;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class History<T> {
    private final Map<Object, HistoryObject<T>> historiesObject = new IdentityHashMap<>();

    public HistoryObject<T> get(Object object) {
        return historiesObject.get(object);
    }

    public HistoryObject<T> put(Object object, HistoryObject<T> historyObject) {
        return historiesObject.put(object, historyObject);
    }

    public static class HistoryObject<T> {
        private final Object object;
        private final List<HistoryCall<T>> historyCalls;
        private final History<T> history;

        public HistoryObject(Object object, List<HistoryCall<T>> historyCalls, History<T> history) {
            this.object = object;
            this.historyCalls = historyCalls;
            this.history = history;
        }

        public Object getObject() {
            return object;
        }

        public List<HistoryCall<T>> getHistoryCalls() {
            return historyCalls;
        }
    }

    public static class HistoryCall<T> {
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
}
