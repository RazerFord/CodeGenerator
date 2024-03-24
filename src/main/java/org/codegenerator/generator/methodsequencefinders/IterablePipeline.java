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
import org.codegenerator.history.HistoryNode;
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
        private final Map<Integer, MethodSequenceFinder> indexToCreator = new HashMap<>();
        private final Set<Class<?>> doNotCache = new HashSet<>();
        private final Deque<IndexedWrapper<RangeResult>> stack = new ArrayDeque<>();
        private final Deque<IndexedWrapper<RangeResult>> ranges = new ArrayDeque<>();
        private final History<Executable> history = new History<>();
        private int lastIndex = -1;
        private final IterablePipeline iterable;
        private final TargetObject targetObject;
        private final Node targetNode;
        private final int nullIndex;

        private CodeIterator(@NotNull TargetObject targetObject, @NotNull IterablePipeline iterable) {
            this.iterable = iterable;
            this.targetObject = targetObject;
            nullIndex = iterable.finderCreators.size() + 1;

            targetNode = ClassFieldExtractor.extract(targetObject.getObject());

            doNotCache.add(targetObject.getClazz());
            indexToCreator.put(nullIndex, iterable.nullFinder);
        }

        @Override
        public boolean hasNext() {
            if (lastIndex >= iterable.finderCreators.size()) {
                return false;
            }
            if (!ranges.isEmpty() || nextFinder(lastIndex + 1)) {
                doNext();
            }
            return !stack.isEmpty();
        }

        @Override
        public @Nullable History<Executable> next() {
            HistoryNode<Executable> node = createHistoryObject();

            history.put(targetObject.getObject(), node);

            TargetObject to = stack.getLast().value.getTo();
            if (compare(to) != 0) {
                useReflection(targetObject, to, history);
            }
            return history;
        }

        private void doNext() {
            while (!stack.isEmpty() && ranges.getLast().index <= stack.getLast().index) {
                stack.pollLast();
            }
            IndexedWrapper<RangeResult> last = tryPollLastFromRanges();
            stack.addLast(last);

            while (compare(last.value.getTo()) != 0) {
                RangeObject rangeObject = new RangeObject(last.value.getTo(), targetObject);
                IndexedWrapper<RangeResultFinding> indexed = findInternal(rangeObject, last.indexOfLastFound + 1);

                if (indexed == null) {
                    ranges.pollLast();
                    return;
                }
                last.indexOfLastFound = indexed.index;

                postProcessing(indexed);

                if (ranges.isEmpty() || indexed.value.getRanges().isEmpty()) {
                    continue;
                }

                last = tryPollLastFromRanges();
                stack.addLast(last);
            }
        }

        private boolean nextFinder(int nextIndex) {
            stack.clear();
            IndexedWrapper<RangeResultFinding> indexed = findInternal(targetObject, nextIndex);

            if (indexed != null) {
                lastIndex = indexed.index;

                postProcessing(indexed);
            } else {
                lastIndex = iterable.finderCreators.size();
            }
            return indexed != null;
        }

        private void postProcessing(@NotNull IndexedWrapper<RangeResultFinding> indexed) {
            indexed.value.getRanges()
                    .forEach(it -> ranges.addLast(new IndexedWrapper<>(indexed.index, it)));

            indexed.value.getSuspects()
                    .stream()
                    .filter(s -> !history.contains(s))
                    .map(TargetObject::new)
                    .map(iterable.pipeline::findReflectionCalls)
                    .forEach(history::merge);
        }

        private @NotNull List<HistoryCall<Executable>> toHistoryCalls(
                History<Executable> history,
                @NotNull RangeResult rangeResult
        ) {
            List<HistoryCall<Executable>> calls = new ArrayList<>();
            for (Edge<? extends Executable> method : rangeResult.getMethods()) {
                calls.add(new HistoryCall<>(history, method.getMethod(), method.getArgs()));
            }
            return calls;
        }

        private int compare(@NotNull TargetObject targetObject) {
            return targetNode.diff(ClassFieldExtractor.extract(targetObject.getObject()));
        }

        private @NotNull IndexedWrapper<RangeResult> tryPollLastFromRanges() {
            IndexedWrapper<RangeResult> last = ranges.getLast();

            if (last.indexOfLastFound >= iterable.finderCreators.size() ||
                    compare(last.value.getTo()) == 0) {
                ranges.pollLast();
            }

            return last;
        }

        private HistoryNode<Executable> createHistoryObject() {
            Iterator<IndexedWrapper<RangeResult>> iterator = stack.descendingIterator();

            HistoryNode<Executable> node = null;
            while (iterator.hasNext()) {
                IndexedWrapper<RangeResult> indexed = iterator.next();
                TargetObject target = indexed.value.getTo();

                node = indexToCreator.get(indexed.index).createNode(
                        target,
                        toHistoryCalls(history, indexed.value),
                        node
                );
            }
            return node;
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

        private @Nullable IndexedWrapper<RangeResultFinding> findInternal(@NotNull Range range, int start) {
            Func func = o -> doFind(range, o);
            Class<?> clazz = range.getTo().getClazz();
            return findInternal(clazz, func, range.getTo(), start);
        }

        private @Nullable IndexedWrapper<RangeResultFinding> findInternal(@NotNull TargetObject target, int start) {
            Func func = o -> doFind(target, o);
            Class<?> clazz = target.getClazz();
            return findInternal(clazz, func, target, start);
        }

        private @NotNull IndexedWrapper<RangeResultFinding> doFind(
                Range range,
                @NotNull IndexedWrapper<MethodSequenceFinder> indexedFinder
        ) {
            RangeResultFinding result = indexedFinder.value.findRanges(range);
            return new IndexedWrapper<>(indexedFinder.index, result);
        }

        private @NotNull IndexedWrapper<RangeResultFinding> doFind(
                TargetObject target,
                @NotNull IndexedWrapper<MethodSequenceFinder> indexedFinder
        ) {
            RangeResultFinding result = indexedFinder.value.findRanges(target);
            return new IndexedWrapper<>(indexedFinder.index, result);
        }

        private @Nullable IndexedWrapper<RangeResultFinding> findInternal(
                Class<?> clazz,
                Func doFind,
                TargetObject target,
                int start
        ) {
            if (iterable.nullFinder.canTry(target)) {
                RangeResultFinding res = iterable.nullFinder.findRanges(target);
                return new IndexedWrapper<>(nullIndex, res);
            }
            IndexedWrapper<MethodSequenceFinder> finder = iterable.cachedFinders.get(clazz);
            if (finder != null) {
                return doFind.apply(finder);
            }
            return tryFinders(clazz, doFind, target, start);
        }

        public @Nullable IndexedWrapper<RangeResultFinding> tryFinders(
                Class<?> clazz,
                Func doFind,
                TargetObject target,
                int start
        ) {
            IndexedWrapper<MethodSequenceFinder> finder = null;
            for (int i = start; i < iterable.finderCreators.size(); i++) {
                try {
                    Function<TargetObject, ? extends MethodSequenceFinder> creator = iterable.finderCreators.get(i);
                    finder = new IndexedWrapper<>(i, creator.apply(target));
                    indexToCreator.put(i, finder.value);
                    if (finder.value.canTry(target)) {
                        IndexedWrapper<RangeResultFinding> res = doFind.apply(finder);
                        cache(clazz, finder);
                        return res;
                    }
                } catch (Exception ignored) {
                    log(finder);
                }
            }
            return null;
        }

        interface Func extends Function<IndexedWrapper<MethodSequenceFinder>, IndexedWrapper<RangeResultFinding>> {
        }

        private static void log(@Nullable IndexedWrapper<MethodSequenceFinder> indexedFinder) {
            if (indexedFinder != null) {
                IterablePipeline.log(indexedFinder.value);
            }
        }
    }

    private static void log(@Nullable MethodSequenceFinder finder) {
        if (finder != null) {
            String className = finder.getClass().getName();
            String msg = String.format("%s failed", className);
            LOGGER.warning(msg);
        }
    }

    private static class IndexedWrapper<T> {
        private final int index;
        private final T value;
        private int indexOfLastFound;

        public IndexedWrapper(int index, T value) {
            this.index = index;
            this.value = value;
            this.indexOfLastFound = index;
        }
    }
}
