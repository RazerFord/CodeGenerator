package org.codegenerator.generator.methodsequencefinders;

import org.codegenerator.generator.codegenerators.buildables.*;
import org.codegenerator.generator.graph.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static org.codegenerator.Utils.throwIf;

public class POJOMethodSequenceFinder {
    private final Class<?> clazz;
    private final StateGraph stateGraph;
    private final PojoConstructorStateGraph pojoConstructorStateGraph;

    public POJOMethodSequenceFinder(@NotNull Class<?> clazz) {
        this.clazz = clazz;
        stateGraph = new StateGraph(clazz);
        pojoConstructorStateGraph = new PojoConstructorStateGraph(clazz);
        checkInvariants();
    }

    public List<Buildable> find(@NotNull Object finalObject) {
        AssignableTypePropertyGrouper assignableTypePropertyGrouper = new AssignableTypePropertyGrouper(finalObject);
        EdgeConstructor edgeConstructor = pojoConstructorStateGraph.findPath(assignableTypePropertyGrouper);
        List<EdgeMethod> methodList = stateGraph.findPath(assignableTypePropertyGrouper, edgeConstructor::invoke);
        return new ArrayList<Buildable>() {
            {
                if (methodList.isEmpty()) {
                    add(new ReturnConstructorCall(clazz, edgeConstructor.getArgs()));
                } else {
                    add(new ConstructorCall(clazz, VARIABLE_NAME, edgeConstructor.getArgs()));
                    addAll(methodList.stream().map(e -> new MethodCall(e.getMethod(), e.getArgs())).collect(Collectors.toList()));
                    add(new Return(VARIABLE_NAME));
                }
            }
        };
    }

    private void checkInvariants() {
        int maxArguments = Arrays.stream(clazz.getDeclaredMethods()).filter(it -> Modifier.isPublic(it.getModifiers())).map(Method::getParameterCount).max(Comparator.naturalOrder()).orElse(0);
        int numberFields = clazz.getDeclaredFields().length;

        throwIf(maxArguments > numberFields, new RuntimeException(NUM_ARG_GREATER_THEN_NUM_FIELDS));
    }

    private static final String VARIABLE_NAME = "object";
    private static final String NUM_ARG_GREATER_THEN_NUM_FIELDS = "The number of arguments is greater than the number of fields";
}