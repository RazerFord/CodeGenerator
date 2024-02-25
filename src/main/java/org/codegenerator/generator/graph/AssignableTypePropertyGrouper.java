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
    private final Class<?> clazz;
    private Supplier<Map<Class<?>, List<Object>>> supplier;

    @Contract(pure = true)
    public AssignableTypePropertyGrouper(@NotNull Object o) {
        clazz = o.getClass();
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

    public Class<?> getClazz() {
        return clazz;
    }

    public Object getObject() {
        return o;
    }

    private @NotNull Map<Class<?>, List<Object>> prepareTypeToValues(@NotNull Object o) {
        Map<Class<?>, List<Object>> typeToValues = new HashMap<>();
        Class<?> clazz1 = clazz;
        while (clazz1 != Object.class) {
            for (Field field : clazz1.getDeclaredFields()) {
                List<Object> list = typeToValues.computeIfAbsent(field.getType(), k -> new ArrayList<>());
                field.setAccessible(true);
                list.add(callSupplierWrapper(() -> field.get(o)));
            }
            clazz1 = clazz1.getSuperclass();
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
}
