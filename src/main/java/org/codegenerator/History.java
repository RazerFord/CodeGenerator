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
        private final Call<T> call;
        private final History<T> history;

        public HistoryCall(Call<T> call, History<T> history) {
            this.call = call;
            this.history = history;
        }

        public Call<T> getCall() {
            return call;
        }

        public HistoryObject<T> getHistoryArg(int index) {
            return history.get(call.getArgs()[index]);
        }
    }
}
