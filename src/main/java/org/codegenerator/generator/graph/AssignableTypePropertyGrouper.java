package org.codegenerator.generator.graph;

import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

import static org.codegenerator.Utils.callSupplierWrapper;

public class AssignableTypePropertyGrouper implements Supplier<Map<Class<?>, List<Object>>> {
    private final Object o;
    private Supplier<Map<Class<?>, List<Object>>> supplier;

    @Contract(pure = true)
    public AssignableTypePropertyGrouper(@NotNull Object o) {
        this.o = o;
        supplier = () -> {
            Map<Class<?>, List<Object>> result = prepareTypeToValues(o);
            supplier = () -> result;
            return result;
        };
    }

    public Map<Class<?>, List<Object>> get() {
        return supplier.get();
    }

    public Object getObject() {
        return o;
    }

    private @NotNull Map<Class<?>, List<Object>> prepareTypeToValues(@NotNull Object o) {
        Class<?> clazz = o.getClass();
        Map<Class<?>, List<Object>> typeToValues = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            List<Object> list = typeToValues.computeIfAbsent(field.getType(), k -> new ArrayList<>());
            field.setAccessible(true);
            list.add(callSupplierWrapper(() -> field.get(o)));
        }
        mergeValuesOfSameTypes(typeToValues);
        return typeToValues;
    }

    @Contract(pure = true)
    private void mergeValuesOfSameTypes(@NotNull Map<Class<?>, List<Object>> typeToValues) {
        for (Class<?> type : typeToValues.keySet()) {
            for (Map.Entry<Class<?>, List<Object>> entry : typeToValues.entrySet()) {
                if (ClassUtils.isAssignable(entry.getKey(), type)) {
                    List<Object> list = typeToValues.get(type);
                    Set<Object> set = new HashSet<>(list);
                    set.addAll(entry.getValue());
                    list.clear();
                    list.addAll(set);
                }
            }
        }
    }
}
