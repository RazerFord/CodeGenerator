package org.codegenerator;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class History<T> {
    private final Map<Object, HistoryObject<T>> historyObject = new IdentityHashMap<>();
    private final HistoryObject<T> rootHistory;

    public History(Object rootObject, List<HistoryCall<T>> historyCalls) {
        rootHistory = new HistoryObject<>(rootObject, historyCalls);
        historyObject.put(rootObject, rootHistory);
    }

    public HistoryObject<T> getRootHistory() {
        return rootHistory;
    }

    public HistoryObject<T> getHistory(Object object) {
        return historyObject.get(object);
    }

    public static class HistoryObject<T> {
        private final Object object;
        private final List<HistoryCall<T>> historyCalls;

        public HistoryObject(Object object, List<HistoryCall<T>> historyCalls) {
            this.object = object;
            this.historyCalls = historyCalls;
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
        private final Call<T> call;

        public HistoryCall(History<T> history, Call<T> call) {
            this.history = history;
            this.call = call;
        }

        public Call<T> getCall() {
            return call;
        }

        public HistoryObject<T> getHistoryArg(int index) {
            return history.getHistory(call.getArgs()[index]);
        }
    }
}
