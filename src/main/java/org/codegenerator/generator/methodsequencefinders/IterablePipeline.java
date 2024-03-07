package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.CustomLogger;
import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.codegenerator.generator.graph.edges.Edge;
import org.codegenerator.generator.graph.resultfinding.RangeResultFinding;
import org.codegenerator.generator.methodsequencefinders.concrete.MethodSequenceFinder;
import org.codegenerator.generator.methodsequencefinders.concrete.NullMethodSequenceFinder;
import org.codegenerator.generator.methodsequencefinders.concrete.ReflectionMethodSequenceFinder;
import org.codegenerator.generator.objectwrappers.Range;
import org.codegenerator.generator.objectwrappers.RangeObject;
import org.codegenerator.generator.objectwrappers.RangeResult;
import org.codegenerator.generator.objectwrappers.TargetObject;
import org.codegenerator.history.History;
import org.codegenerator.history.HistoryCall;
import org.codegenerator.history.HistoryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Executable;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

public class IterablePipeline implements Iterable<History<Executable>> {
    private static final Logger LOGGER = CustomLogger.getLogger();

    private final ReflectionMethodSequenceFinder reflectionFinder = new ReflectionMethodSequenceFinder();
    private final NullMethodSequenceFinder nullFinder = new NullMethodSequenceFinder();
    private final Map<Class<?>, IndexedWrapper<MethodSequenceFinder>> cachedFinders = new HashMap<>();
    private final List<Function<TargetObject, ? extends MethodSequenceFinder>> finderCreators;
    private final TargetObject targetObject;
    private final MethodSequencePipeline pipeline;

    public IterablePipeline(
            List<Function<TargetObject, ? extends MethodSequenceFinder>> finderCreators,
            TargetObject targetObject
    ) {
        this.finderCreators = finderCreators;
        this.targetObject = targetObject;

        pipeline = new PipelineMethodSequencePipeline(finderCreators);
    }

    @NotNull
    @Override
    public Iterator<History<Executable>> iterator() {
        return new CodeIterator(targetObject, this);
    }

    private static class CodeIterator implements Iterator<History<Executable>> {
        private final Map<Integer, Class<?>> indexToCreator = new HashMap<>();
        private final Set<Class<?>> doNotCache = new HashSet<>();
        private final Deque<IndexedWrapper<RangeResult>> stack = new ArrayDeque<>();
        private final Deque<IndexedWrapper<RangeResult>> allRanges = new ArrayDeque<>();
        private final Deque<IndexedWrapperResult> found = new ArrayDeque<>();
        private final History<Executable> history = new History<>();
        private int lastIndex = -1;
        private final TargetObject targetObject;
        private final IterablePipeline iterable;
        private final Node targetNode;

        private CodeIterator(@NotNull TargetObject targetObject, @NotNull IterablePipeline iterable) {
            this.targetObject = targetObject;
            this.iterable = iterable;

            targetNode = ClassFieldExtractor.extract(targetObject.getObject());

            doNotCache.add(targetObject.getClazz());
        }

        @Override
        public boolean hasNext() {
            if (lastIndex >= iterable.finderCreators.size()) {
                return false;
            }
            if (!allRanges.isEmpty() || nextFinder(lastIndex + 1)) {
                while (!stack.isEmpty() && allRanges.getLast().index <= stack.getLast().index) {
                    stack.pollLast();
                }
                IndexedWrapper<RangeResult> last = allRanges.pollLast();
                stack.addLast(last);
                while (compare(last.value.getTo().getObject()) != 0) {
                    RangeObject rangeObject = new RangeObject(last.value.getTo(), targetObject);
                    IndexedWrapper<RangeResultFinding> indexed = findInternal(rangeObject, last.index + 1);

                    if (indexed == null) return !stack.isEmpty();
                    indexed.value.getRanges().forEach(it -> allRanges.addLast(new IndexedWrapper<>(indexed.index, it)));

                    if (indexed.value.getRanges().isEmpty()) return !stack.isEmpty();
                    last = allRanges.pollLast();

                    stack.addLast(last);
                }
            }
            return !stack.isEmpty();
        }

        @Override
        public @Nullable History<Executable> next() {
            Iterator<IndexedWrapper<RangeResult>> iterator = stack.descendingIterator();

            HistoryObject<Executable> node = null;
            while (iterator.hasNext()) {
                IndexedWrapper<RangeResult> indexed = iterator.next();
                Object target = indexed.value.getTo().getObject();

                node = new HistoryObject<>(target,
                        toHistoryCalls(history, indexed.value),
                        indexToCreator.get(indexed.index),
                        node
                );
            }
            history.put(targetObject.getObject(), node);
            TargetObject to = stack.getLast().value.getTo();
            if (compare(to) != 0) {
                useReflection(targetObject, to, history);
            }
            return history;
        }

        private boolean nextFinder(int nextIndex) {
            IndexedWrapper<RangeResultFinding> indexed = findInternal(targetObject, nextIndex);

            if (indexed != null) {
                found.addLast(new IndexedWrapperResult(indexed));
                lastIndex = indexed.index;
                indexed.value.getSuspects().stream()
                        .filter(s -> !history.contains(s))
                        .map(TargetObject::new)
                        .map(iterable.pipeline::findReflectionCalls)
                        .forEach(history::merge);
                indexed.value.getRanges().forEach(it -> allRanges.addLast(new IndexedWrapper<>(indexed.index, it)));
                stack.clear();
            } else {
                lastIndex = iterable.finderCreators.size();
            }
            return indexed != null;
        }

        private @NotNull List<HistoryCall<Executable>> toHistoryCalls(History<Executable> history, @NotNull RangeResult rangeResult) {
            List<HistoryCall<Executable>> calls = new ArrayList<>();
            for (Edge<? extends Executable> method : rangeResult.getMethods()) {
                calls.add(new HistoryCall<>(history, method.getMethod(), method.getArgs()));
            }
            return calls;
        }

        private @Nullable IndexedWrapper<RangeResultFinding> findInternal(Range range, int start) {
            if (iterable.nullFinder.canTry(range)) {
                RangeResultFinding res = iterable.nullFinder.findRanges(range);
                return new IndexedWrapper<>(-1, res);
            }
            Class<?> clazz = range.getTo().getClazz();
            IndexedWrapper<MethodSequenceFinder> finder = iterable.cachedFinders.get(clazz);
            if (finder != null) {
                return doFind(range, finder);
            }
            for (int i = start; i < iterable.finderCreators.size(); i++) {
                try {
                    Function<TargetObject, ? extends MethodSequenceFinder> creator = iterable.finderCreators.get(i);
                    finder = new IndexedWrapper<>(i, creator.apply(range.getTo()));
                    indexToCreator.put(i, finder.value.getClass());
                    if (finder.value.canTry(range)) {
                        @NotNull IndexedWrapper<RangeResultFinding> res = doFind(range, finder);
                        cache(clazz, finder);
                        return res;
                    }
                } catch (Exception ignored) {
                    logging(finder);
                }
            }
            return null;
        }

        private @NotNull IndexedWrapper<RangeResultFinding> doFind(
                Range range,
                @NotNull IndexedWrapper<MethodSequenceFinder> indexedFinder
        ) {
            RangeResultFinding res = indexedFinder.value.findRanges(range);
            return new IndexedWrapper<>(indexedFinder.index, res);
        }

        private @Nullable IndexedWrapper<RangeResultFinding> findInternal(TargetObject target, int start) {
            if (iterable.nullFinder.canTry(target)) {
                RangeResultFinding res = iterable.nullFinder.findRanges(target);
                return new IndexedWrapper<>(-1, res);
            }
            Class<?> clazz = target.getClazz();
            IndexedWrapper<MethodSequenceFinder> finder = iterable.cachedFinders.get(clazz);
            if (finder != null) {
                return doFind(target, finder);
            }
            for (int i = start; i < iterable.finderCreators.size(); i++) {
                try {
                    Function<TargetObject, ? extends MethodSequenceFinder> creator = iterable.finderCreators.get(i);
                    finder = new IndexedWrapper<>(i, creator.apply(target));
                    indexToCreator.put(i, finder.value.getClass());
                    if (finder.value.canTry(target)) {
                        @NotNull IndexedWrapper<RangeResultFinding> res = doFind(target, finder);
                        cache(clazz, finder);
                        return res;
                    }
                } catch (Exception ignored) {
                    logging(finder);
                }
            }
            return null;
        }

        private @NotNull IndexedWrapper<RangeResultFinding> doFind(
                TargetObject target,
                @NotNull IndexedWrapper<MethodSequenceFinder> indexedFinder
        ) {
            RangeResultFinding res = indexedFinder.value.findRanges(target);
            return new IndexedWrapper<>(indexedFinder.index, res);
        }

        private int compare(Object object) {
            return targetNode.diff(ClassFieldExtractor.extract(object));
        }

        private void useReflection(
                TargetObject expected,
                TargetObject actual,
                @NotNull History<Executable> history
        ) {
            iterable.reflectionFinder.findSetter(expected, actual, history)
                    .getSuspects()
                    .stream()
                    .filter(s -> !history.contains(s))
                    .map(TargetObject::new)
                    .map(iterable.pipeline::findReflectionCalls)
                    .forEach(history::merge);
        }

        private void cache(Class<?> clazz, IndexedWrapper<MethodSequenceFinder> finder) {
            if (!doNotCache.contains(clazz)) {
                iterable.cachedFinders.put(clazz, finder);
            }
        }
    }

    private static <K, V> void putIfNotExists(@NotNull Map<K, List<V>> map, K k, V v) {
        List<V> list = map.computeIfAbsent(k, k1 -> new ArrayList<>());
        list.add(v);
    }

    private static void logging(@Nullable MethodSequenceFinder finder) {
        if (finder != null) {
            String className = finder.getClass().getName();
            String msg = String.format("%s failed", className);
            LOGGER.warning(msg);
        }
    }

    private static void logging(@Nullable IndexedWrapper<MethodSequenceFinder> indexedFinder) {
        if (indexedFinder != null) {
            logging(indexedFinder.value);
        }
    }

    private static class IndexedWrapperResult implements Iterator<RangeResult> {
        private boolean isFirst = true;
        private boolean isFound = false;
        private final int index;
        private final RangeResultFinding result;
        private final Iterator<RangeResult> iterator;

        public IndexedWrapperResult(@NotNull IndexedWrapper<RangeResultFinding> result) {
            this.index = result.index;
            this.result = result.value;
            iterator = result.value.getRanges().iterator();
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public RangeResult next() {
            isFirst = false;
            return iterator.next();
        }

        public boolean isFound() {
            return isFound;
        }

        public boolean isFirst() {
            return isFirst;
        }
    }

    private static class IndexedWrapper<T> {
        private final int index;
        private final T value;

        public IndexedWrapper(int index, T value) {
            this.index = index;
            this.value = value;
        }
    }
}
