package org.codegenerator.generator.graph;

import kotlin.Pair;
import org.codegenerator.extractor.ClassFieldExtractor;
import org.codegenerator.extractor.node.Node;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.codegenerator.Utils.*;

public class PojoConstructorStateGraph {
    private final Class<?> clazz;
    private final EdgeGeneratorConstructor edgeGeneratorConstructor;

    public PojoConstructorStateGraph(Class<?> clazz) {
        this.clazz = clazz;
        edgeGeneratorConstructor = new EdgeGeneratorConstructor(clazz);
    }

    public @NotNull Pair<Object, Function<Object, Object>> findPath(Object finalObject) {
        AssignableTypePropertyGrouper assignableTypePropertyGrouper = new AssignableTypePropertyGrouper(finalObject);
        @NotNull Map<Class<?>, List<Object>> values = assignableTypePropertyGrouper.get();
        List<EdgeConstructor> edgeConstructors = edgeGeneratorConstructor.generate(values);

        Pair<Object, EdgeConstructor> objectEdgeConstructorPair = buildBeginObjectAndMethodCall(finalObject, edgeConstructors);
        Object beginObject = objectEdgeConstructorPair.getFirst();
        EdgeConstructor edgeConstructor = objectEdgeConstructorPair.getSecond();
        Function<Object, Object> copyObject = copyObject(edgeConstructor::invoke);

        return new Pair<>(beginObject, copyObject);
    }

    private @NotNull Pair<Object, EdgeConstructor> buildBeginObjectAndMethodCall(Object finalObject, @NotNull List<EdgeConstructor> edges) {
        Node finalNode = ClassFieldExtractor.extract(finalObject);
        throwIf(edges.isEmpty(), new RuntimeException(NO_CONSTRUCTORS));
        EdgeConstructor edgeConstructor = edges.get(0);
        Object currObject = edgeConstructor.invoke();
        Node currNode = ClassFieldExtractor.extract(currObject);
        for (int i = 1, length = edges.size(); i < length; i++) {
            EdgeConstructor tempEdgeConstructor = edges.get(i);
            Object tempObject = tempEdgeConstructor.invoke();
            Node tempNode = ClassFieldExtractor.extract(tempObject);
            if (finalNode.diff(currNode) > finalNode.diff(tempNode)) {
                edgeConstructor = tempEdgeConstructor;
                currObject = tempObject;
                currNode = tempNode;
            }
        }
        return new Pair<>(currObject, edgeConstructor);
    }

    @Contract(pure = true)
    private @NotNull Function<Object, Object> copyObject(@NotNull Supplier<Object> supplier) {
        return (o) -> {
            Object instance = callSupplierWrapper(supplier::get);
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                callRunnableWrapper(() -> field.set(instance, callSupplierWrapper(() -> field.get(o))));
            }
            return instance;
        };
    }

    private static final String NO_CONSTRUCTORS = "No constructors found";
}
