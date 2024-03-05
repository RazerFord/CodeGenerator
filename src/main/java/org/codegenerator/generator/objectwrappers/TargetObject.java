package org.codegenerator.generator.objectwrappers;

import org.apache.commons.lang3.ClassUtils;
import org.codegenerator.extractor.node.Node;
import org.codegenerator.extractor.node.ValueCollectorImpl;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

public class TargetObject {
    private final Object o;
    private final Class<?> clazz;
    private Supplier<Map<Class<?>, List<Object>>> supplier;

    @Contract(pure = true)
    public TargetObject(Object o) {
        this.o = o;

        clazz = o != null ? o.getClass() : null;

        supplier = createLazySupplier();
    }

    public Map<Class<?>, List<Object>> get() {
        return supplier.get();
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Object getObject() {
        return o;
    }

    private @NotNull Map<Class<?>, List<Object>> prepareTypeToValues(@NotNull Object o) {
        Map<Class<?>, List<Object>> typeToValues = new HashMap<>();
        ValueCollectorImpl visitor = new ValueCollectorImpl();
        Node.createNode(o).accept(visitor);
        for (Map.Entry<Class<?>, List<Object>> e : visitor.getTypeToValues().entrySet()) {
            for (Object o1 : e.getValue()) {
                List<Object> list = typeToValues.computeIfAbsent(e.getKey(), k -> new ArrayList<>());
                list.add(o1);
            }
        }
        mergeValuesOfSameTypes(typeToValues);
        return typeToValues;
    }

    @Contract(pure = true)
    private void mergeValuesOfSameTypes(@NotNull Map<Class<?>, List<Object>> typeToValues) {
        for (Map.Entry<Class<?>, List<Object>> entry : typeToValues.entrySet()) {
            for (Map.Entry<Class<?>, List<Object>> entryInner : typeToValues.entrySet()) {
                Class<?> type = entry.getKey();
                if (ClassUtils.isAssignable(entryInner.getKey(), type)) {
                    List<Object> list = entry.getValue();
                    Set<Object> set = new HashSet<>(list);
                    set.addAll(entryInner.getValue());
                    list.clear();
                    list.addAll(set);
                }
            }
        }
    }

    @Contract(pure = true)
    private @NotNull Supplier<Map<Class<?>, List<Object>>> createLazySupplier() {
        return () -> {
            if (o == null || ClassUtils.isPrimitiveOrWrapper(o.getClass())) {
                return Collections.emptyMap();
            }
            Map<Class<?>, List<Object>> result = prepareTypeToValues(o);
            supplier = () -> result;
            return result;
        };
    }
}
