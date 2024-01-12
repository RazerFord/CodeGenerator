package org.codegenerator.generator.codegenerators;

import org.codegenerator.generator.graph.StateGraph;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.codegenerator.Utils.*;

public class POJOGraphPathSearch {
    private final Class<?> clazz;
    private final Constructor<?> defaultConstructor;
    private final StateGraph stateGraph;

    public POJOGraphPathSearch(@NotNull Class<?> clazz) {
        this.clazz = clazz;
        stateGraph = new StateGraph(clazz);
        defaultConstructor = getConstructorWithoutArgs();
        checkInvariants();
    }

    public List<MethodCall> find(@NotNull Object finalObject) {
        Object beginObject = callSupplierWrapper(defaultConstructor::newInstance);
        return stateGraph.findPath(beginObject, finalObject, this::copyObject);
    }

    private Object copyObject(Object o) {
        Object instance = callSupplierWrapper(defaultConstructor::newInstance);
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            callRunnableWrapper(() -> field.set(instance, callSupplierWrapper(() -> field.get(o))));
        }
        return instance;
    }

    private @Nullable Constructor<?> getConstructorWithoutArgs() {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 0) {
                return constructor;
            }
        }
        return null;
    }

    private void checkInvariants() {
        throwIf(defaultConstructor == null, new RuntimeException(NO_CONSTRUCTOR_WITHOUT_ARG));

        int maxArguments = Arrays.stream(clazz.getDeclaredMethods()).filter(it -> Modifier.isPublic(it.getModifiers())).map(Method::getParameterCount).max(Comparator.naturalOrder()).orElse(0);
        int numberFields = clazz.getDeclaredFields().length;

        throwIf(maxArguments > numberFields, new RuntimeException(NUM_ARG_GREATER_THEN_NUM_FIELDS));
    }

    private static final String NO_CONSTRUCTOR_WITHOUT_ARG = "There is no constructor without arguments";
    private static final String NUM_ARG_GREATER_THEN_NUM_FIELDS = "The number of arguments is greater than the number of fields";
}
