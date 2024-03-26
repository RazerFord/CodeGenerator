package org.codegenerator.generator.graph;

import org.apache.commons.lang3.mutable.MutableObject;
import org.codegenerator.CommonUtils;
import org.codegenerator.CustomLogger;
import org.codegenerator.exceptions.MethodSequenceNotFoundException;
import org.codegenerator.generator.BuilderInfo;
import org.codegenerator.generator.graph.edges.Edge;
import org.codegenerator.generator.graph.edges.EdgeExecutable;
import org.codegenerator.generator.graph.edges.EdgeMethod;
import org.codegenerator.generator.objectwrappers.Range;
import org.codegenerator.generator.objectwrappers.RangeObject;
import org.codegenerator.generator.objectwrappers.TargetObject;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class LazyGraphCombinationBuilder {
    private final LazyMethodGraph methodGraph = new LazyMethodGraph();
    private static final Logger LOGGER = CustomLogger.getLogger();

    private BuilderInfo builderInfo;
    private Consumer<Consumer<BuilderInfo>> finder;

    public LazyGraphCombinationBuilder(List<BuilderInfo> builderInfoList) {
        initFinder(builderInfoList);
    }

    public @NotNull Path findPath(@NotNull TargetObject targetObject) {
        MutableObject<Path> ref = new MutableObject<>();
        finder.accept(bi -> {
            ref.setValue(find(bi, targetObject));
            builderInfo = bi;
        });
        return ref.getValue();
    }

    public @NotNull Path findPath(@NotNull Range range) {
        MutableObject<Path> ref = new MutableObject<>();
        finder.accept(bi -> {
            ref.setValue(find(bi, range, new ArrayList<>()));
            builderInfo = bi;
        });
        return ref.getValue();
    }

    /**
     * Returns information about the builder that was used when searching
     * for a sequence of methods.
     *
     * @return information about the builder
     * @throws NullPointerException if this method was called before
     *                              searching for a sequence of methods
     */
    public @NotNull BuilderInfo getBuilderInfo() {
        return Objects.requireNonNull(builderInfo);
    }

    private @NotNull Path find(
            @NotNull BuilderInfo bi,
            Range range,
            List<Edge<? extends Executable>> methods
    ) {
        EdgeMethod termination = new EdgeMethod(bi.method());
        methodGraph.setTermination(termination);
        Path path = methodGraph.findPath(range, methods);
        methods.add(termination);
        return path;
    }

    private @NotNull Path find(
            @NotNull BuilderInfo bi,
            TargetObject targetObject
    ) {
        Object from = createConstructorSupplier(bi.constructor()).get();
        Edge<? extends Executable> ctor = new EdgeExecutable(bi.constructor());
        List<Edge<? extends Executable>> methods = new ArrayList<>(Collections.singletonList(ctor));
        return find(bi, new RangeObject(from, targetObject), methods);
    }

    private void initFinder(List<BuilderInfo> builderInfoList) {
        finder = (Consumer<BuilderInfo> consumer) -> {
            for (BuilderInfo builderInfo1 : builderInfoList) {
                try {
                    consumer.accept(builderInfo1);
                    finder = (Consumer<BuilderInfo> consumer1)
                            -> consumer1.accept(builderInfo1);
                    return;
                } catch (Exception e) {
                    Class<?> type = builderInfo1.builder();
                    LOGGER.warning(String.format("Unsuccessful use of the builder %s", type));
                }
            }
            throw new MethodSequenceNotFoundException();
        };
    }

    @Contract(pure = true)
    private static @NotNull Supplier<Object> createConstructorSupplier(Executable executable) {
        if (executable instanceof Method) {
            return () -> CommonUtils.callSupplierWrapper(() -> ((Method) executable).invoke(null));
        }
        if (executable instanceof Constructor<?>) {
            return () -> CommonUtils.callSupplierWrapper(((Constructor<?>) executable)::newInstance);
        }
        throw new IllegalArgumentException();
    }
}
