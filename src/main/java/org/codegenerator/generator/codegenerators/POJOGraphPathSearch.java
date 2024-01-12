package org.codegenerator.generator.codegenerators;

import org.codegenerator.generator.codegenerators.buildables.Buildable;
import org.codegenerator.generator.graph.StateGraph;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.codegenerator.Utils.throwIf;

public class POJOGraphPathSearch {
    private final Class<?> clazz;
    private final StateGraph stateGraph;

    public POJOGraphPathSearch(@NotNull Class<?> clazz) {
        this.clazz = clazz;
        stateGraph = new StateGraph(clazz);
        checkInvariants();
    }

    public List<Buildable> find(@NotNull Object finalObject) {
        return stateGraph.findPath(finalObject);
    }

    private void checkInvariants() {
        int maxArguments = Arrays.stream(clazz.getDeclaredMethods()).filter(it -> Modifier.isPublic(it.getModifiers())).map(Method::getParameterCount).max(Comparator.naturalOrder()).orElse(0);
        int numberFields = clazz.getDeclaredFields().length;

        throwIf(maxArguments > numberFields, new RuntimeException(NUM_ARG_GREATER_THEN_NUM_FIELDS));
    }

    private static final String NUM_ARG_GREATER_THEN_NUM_FIELDS = "The number of arguments is greater than the number of fields";
}
